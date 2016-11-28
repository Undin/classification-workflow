package com.warrior.classification_workflow.meta_learning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.meta_learning.calculators.ClassifierPerformanceEvaluator
import com.warrior.classification_workflow.meta_learning.calculators.Evaluator
import com.warrior.classification_workflow.meta_learning.calculators.MetaFeatureEvaluator
import com.warrior.classification_workflow.meta_learning.calculators.TransformerPerformanceEvaluator
import libsvm.svm
import org.apache.commons.cli.*
import java.io.File
import java.util.*

/**
 * Created by warrior on 11/14/16.
 */
fun main(args: Array<String>) {
    val (metaFeatureConfigPath, classifierPerfConfigPath, transformerPerfConfig) = parseArgs(args)
    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    val evaluators = ArrayList<Evaluator>()
    if (metaFeatureConfigPath != null) {
        val config: MetaFeatureConfig = mapper.readValue(File(metaFeatureConfigPath))
        evaluators += MetaFeatureEvaluator(config)
    }
    if (classifierPerfConfigPath != null) {
        val config: ClassifierPerfConfig = mapper.readValue(File(classifierPerfConfigPath))
        evaluators += ClassifierPerformanceEvaluator(config)
    }
    if (transformerPerfConfig != null) {
        val config: TransformerPerfConfig = mapper.readValue(File(transformerPerfConfig))
        evaluators += TransformerPerformanceEvaluator(config)
    }

    // disable svm logs
    svm.svm_set_print_string_function { it -> }
    evaluators.forEach { it.evaluate() }
}

private fun parseArgs(args: Array<String>): ConfigPaths {
    val metaFeatureConfigOption = Option.builder("m")
            .longOpt("meta-feature-config")
            .hasArg(true)
            .argName("path")
            .desc("path to meta-feature extraction config_name.yaml file")
            .build()
    val classifierPerfConfigOption = Option.builder("c")
            .longOpt("classifier-perf-config")
            .hasArg(true)
            .argName("path")
            .desc("path to classifier performance measurement config_name.yaml file")
            .build()
    val transformerPerfConfigOption = Option.builder("t")
            .longOpt("transformer-perf-config")
            .hasArg(true)
            .argName("path")
            .desc("path to transformer performance measurement config_name.yaml file")
            .build()
    val helpOption = Option.builder("h")
            .longOpt("help")
            .desc("show this help")
            .build()
    val allOptions = options(metaFeatureConfigOption, classifierPerfConfigOption,
            transformerPerfConfigOption, helpOption)

    val parser = DefaultParser()
    val line = try {
        parser.parse(options(helpOption), args, false)
    } catch (e: ParseException) {
        null
    }
    if (line != null && line.hasOption(helpOption.opt)) {
        printHelp(allOptions)
        System.exit(0)
    } else {
        try {
            val configOptions = options(metaFeatureConfigOption, classifierPerfConfigOption, transformerPerfConfigOption)
            val line = parser.parse(configOptions, args)
            return ConfigPaths(
                    metaFeatureConfigPath = line.getOptionValue(metaFeatureConfigOption.opt),
                    classifierPerfConfigPath = line.getOptionValue(classifierPerfConfigOption.opt),
                    transformerPerfConfigPath = line.getOptionValue(transformerPerfConfigOption.opt)
            )
        } catch (e: ParseException) {
            println(e.message)
            printHelp(allOptions)
            System.exit(1)
        }
    }
    // unreachable
    return ConfigPaths(null, null, null)
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

data class ConfigPaths(
        val metaFeatureConfigPath: String?,
        val classifierPerfConfigPath: String?,
        val transformerPerfConfigPath: String?
)
