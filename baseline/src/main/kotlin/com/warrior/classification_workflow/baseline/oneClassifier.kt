package com.warrior.classification_workflow.baseline

import com.fasterxml.jackson.databind.ObjectMapper
import com.warrior.classification_workflow.core.Algorithm.Classifier
import com.warrior.classification_workflow.core.AlgorithmConfiguration.ClassifierConfiguration
import com.warrior.classification_workflow.core.Workflow
import com.warrior.classification_workflow.core.load
import kotlinx.support.jdk8.collections.parallelStream
import libsvm.svm
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import weka.classifiers.evaluation.Evaluation
import weka.core.Instances
import weka.gui.ExtensionFileFilter
import java.io.File
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

/**
 * Created by warrior on 17/07/16.
 */

val RESULT_FOLDER = "baseline-results"

fun main(args: Array<String>) {
    val arguments = Args()
    val parser = CmdLineParser(arguments)
    try {
        parser.parseArgument(*args)
        val mapper = ObjectMapper()
        val config = mapper.readValue(File(arguments.configPath), Config::class.java)
        doClassification(config)
    } catch (e: CmdLineException) {
        e.printStackTrace(System.err)
        parser.printUsage(System.err)
    }
}

private fun doClassification(config: Config) {
    // disable libSVM logs
    svm.svm_set_print_string_function { it -> }

    val files = File("datasets").listFiles(ExtensionFileFilter("arff", "")) ?: return

    File(RESULT_FOLDER).mkdir()
    val saveStrategy = when (config.saveStrategy) {
        "json" -> SaveStrategy.JsonSaveStrategy("${RESULT_FOLDER}/result-${System.currentTimeMillis()}.json")
        "database" -> SaveStrategy.DatabaseSaveStrategy()
        else -> throw IllegalArgumentException("unknown value for save strategy: ${config.saveStrategy}")
    }
    saveStrategy.use { saveStrategy ->
        val pool = ForkJoinPool(config.threads)
        val tasks = ArrayList<ForkJoinTask<*>>()
        if (config.workflows != null) {
            tasks += pool.submit { calculate(config.workflows, files, saveStrategy) }
        }
        if (config.configurations != null) {
            config.configurations.parallelStream()
                    .forEach {
                        tasks += pool.submit { calculate(it.generateAllWorkflows(), files, saveStrategy) }
                    }
        }
        for (task in tasks) {
            task.get()
        }
    }
}

private fun ClassifierConfiguration.generateAllWorkflows(): List<Workflow> {

    val keys = classifierOptions.keys.toList()

    fun generate(k: Int, options: MutableMap<String, String>, out: MutableList<Workflow>) {
        if (k == keys.size) {
            val classifier = Classifier(name, classifierClass, HashMap(options))
            val workflow = com.warrior.classification_workflow.core.Workflow(emptyList(), classifier)
            out += workflow
        } else {
            val key = keys[k]
            val values = classifierOptions[key]!!
            for (value in values) {
                options[key] = value
                generate(k + 1, options, out)
                options.remove(key)
            }
        }
    }

    val options = HashMap<String, String>(keys.size)
    val out = ArrayList<Workflow>()
    generate(0, options, out)
    return out
}

private fun calculate(workflows: List<Workflow>, files: Array<File>, saveStrategy: SaveStrategy) {
    Arrays.stream(files)
            .parallel()
            .forEach { file ->
                val data = load(file.absolutePath)
                calculate(workflows, data, file.nameWithoutExtension, saveStrategy)
            }
}

private fun calculate(workflows: List<Workflow>, data: Instances, name: String, saveStrategy: SaveStrategy) {
    workflows.parallelStream()
            .forEach { workflow ->
                println("start: ${workflow.classifier.name} on $name")
                val result = measure(workflow, data, name)
                println("end: ${workflow.classifier.name} on $name")
                saveStrategy.save(result)
            }
}

private fun measure(workflow: Workflow, data: Instances, name: String): ResultEntity {
    val eval = Evaluation(data)
    eval.crossValidateModel(workflow, data, 10, Random())
    return ResultEntity(name, workflow, "f-measure", eval.unweightedMacroFmeasure())
}

class Args {
    @Option(name = "-c", required = true, usage = "path to config file") lateinit var configPath: String
}
