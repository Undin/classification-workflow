package com.warrior.classification_workflow.baseline.single

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.baseline.normalize
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.core.storage.SaveStrategy
import com.warrior.classification_workflow.core.subInstances
import libsvm.svm
import org.apache.commons.cli.*
import java.io.File
import java.util.*
import java.util.concurrent.ForkJoinPool

/**
 * Created by warrior on 2/1/17.
 */
private const val MAX_INSTANCES = 5000

fun main(args: Array<String>) {
    val configFile = parseArgs(args)

    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: TuningConfig = yamlMapper.readValue(File(configFile))
    val pool = ForkJoinPool(config.threads)
    val random = Random()

    svm.svm_set_print_string_function {  }

    val saveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)
    saveStrategy.use {
        for (datasetFile in config.datasets) {
            val datasetName = File(datasetFile).nameWithoutExtension
            val data = subInstances(load("${config.datasetFolder}/$datasetFile").normalize(), MAX_INSTANCES, random)
            for ((baseClassifier, params) in config.algorithms) {
                val search = TwoStepGridSearch(baseClassifier, data, 10, pool, random)
                val (bestParams, score) = search.search(params)
                val result = SingleClassifierTuningEntity(baseClassifier.name, datasetName, bestParams, score)
                saveStrategy.save(result)
            }
        }
    }
}

private fun parseArgs(args: Array<String>): String {
    val configOption = Option.builder("c")
            .longOpt("config")
            .hasArg(true)
            .argName("path")
            .desc("path to config_name.yaml file")
            .build()
    val helpOption = Option.builder("h")
            .longOpt("help")
            .desc("show this help")
            .build()
    val options = options(configOption, helpOption)
    val parser = DefaultParser()

    val line = try {
        parser.parse(options, args)
    } catch (e: ParseException) {
        println(e.message)
        printHelp(options)
        System.exit(1)
        // unreachable
        throw IllegalStateException()
    }

    if (line.hasOption(helpOption.opt) || !line.hasOption(configOption.opt)) {
        printHelp(options)
        System.exit(0)
    } else {
        return line.getOptionValue(configOption.opt)
    }
    // unreachable
    throw IllegalStateException()
}

private fun options(vararg opts: Option): Options {
    val options = Options()
    for (opt in opts) {
        options.addOption(opt)
    }
    return options
}

private fun printHelp(options: Options) {
    val formatter = HelpFormatter()
    formatter.printHelp("java -jar jarfile.jar [options...]", options)
}
