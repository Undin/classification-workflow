package com.warrior.classification.workflow.baseline

import com.fasterxml.jackson.databind.ObjectMapper
import com.warrior.classification.workflow.core.Workflow
import com.warrior.classification.workflow.core.load
import org.hibernate.cfg.Configuration
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import weka.classifiers.Classifier
import weka.classifiers.evaluation.Evaluation
import weka.core.Instances
import weka.gui.ExtensionFileFilter
import java.io.File
import java.util.*
import java.util.concurrent.ForkJoinPool

/**
 * Created by warrior on 17/07/16.
 */
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
    libsvm.svm.svm_set_print_string_function { it ->  }

    val files = File("datasets").listFiles(ExtensionFileFilter("arff", ""))
    if (files != null) {
        val sessionFactory = Configuration()
                .configure()
                .buildSessionFactory()
        val session = sessionFactory.openSession()

        try {
            val pool = ForkJoinPool(config.threads)
            pool.submit({
                Arrays.stream(files)
                        .parallel()
                        .forEach { file ->
                            val data = load(file.absolutePath)
                            println("start ${file.nameWithoutExtension}")
                            val result = measure(config.workflow, data, file.nameWithoutExtension)
                            session.saveInTransaction(result)
                            println("end ${file.nameWithoutExtension}")
                        }
            }).get()
        } finally {
            session.close()
            sessionFactory.close()
        }
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
