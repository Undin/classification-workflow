package com.warrior.classification_workflow.meta_learning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.meta_learning.metafeatures.MetaFeatureExtractor
import kotlinx.support.jdk8.collections.parallelStream
import libsvm.svm
import org.apache.commons.cli.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.Supplier
import weka.classifiers.Evaluation
import weka.core.Instances
import java.io.File
import java.util.*

/**
 * Created by warrior on 11/14/16.
 */
val logger = LogManager.getLogger()

fun main(args: Array<String>) {
    val (metaFeatureConfigPath, performanceConfigPath) = parseArgs(args)
    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    if (metaFeatureConfigPath != null) {
        val config: MetaFeatureConfig = mapper.readValue(File(metaFeatureConfigPath))
        extractMetaFeatures(config)
    }
    if (performanceConfigPath != null) {
        val config: PerformanceConfig = mapper.readValue(File(performanceConfigPath))
        measurePerformance(config)
    }
}

private fun parseArgs(args: Array<String>): ConfigPaths {
    val helpOption = Option.builder("h")
            .longOpt("help")
            .desc("show this help")
            .build()
    val metaFeatureConfigOption = Option.builder("m")
            .longOpt("meta-feature-config")
            .hasArg(true)
            .argName("path")
            .desc("path to meta-feature extraction config_name.yaml file")
            .build()
    val performanceConfigOption = Option.builder("p")
            .longOpt("performance-config")
            .hasArg(true)
            .argName("path")
            .desc("path to performance measurement config_name.yaml file")
            .build()


    val parser = DefaultParser()
    val line = try {
        parser.parse(options(helpOption), args, false)
    } catch (e: ParseException) {
        null
    }
    if (line != null && line.hasOption(helpOption.opt)) {
        printHelp(helpOption, metaFeatureConfigOption, performanceConfigOption)
        System.exit(0)
    } else {
        try {
            val line = parser.parse(options(metaFeatureConfigOption, performanceConfigOption), args)
            return ConfigPaths(
                    metaFeatureConfigPath = line.getOptionValue(metaFeatureConfigOption.opt),
                    performanceConfigPath = line.getOptionValue(performanceConfigOption.opt)
            )
        } catch (e: ParseException) {
            println(e.message)
            printHelp(helpOption, metaFeatureConfigOption, performanceConfigOption)
            System.exit(1)
        }
    }
    // unreachable
    return ConfigPaths(null, null)
}

data class ConfigPaths(val metaFeatureConfigPath: String?, val performanceConfigPath: String?)

private fun options(vararg opts: Option): Options {
    val options = Options()
    for (opt in opts) {
        options.addOption(opt)
    }
    return options
}

private fun printHelp(vararg opts: Option) {
    val formatter = HelpFormatter()
    formatter.printHelp("java -jar jarfile.jar [options...]", options(*opts))
}

private fun measurePerformance(config: PerformanceConfig) {
    val datasets = config.datasets.map { File(config.datasetFolder, it) }
    val saveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)

    svm.svm_set_print_string_function { it -> }
    val random = Random()
    saveStrategy.use { saveStrategy ->
        datasets.parallelStream()
                .forEach { dataset ->
                    val data = load(dataset.absolutePath)
                    config.classifiers.parallelStream()
                            .forEach { classifier ->
                                calculate(classifier, data, random, saveStrategy)
                            }
                }

    }
}

private fun calculate(classifier: Classifier, data: Instances, random: Random, saveStrategy: SaveStrategy) {
    logger.info(Supplier { "start evaluate ${classifier.name} on ${data.relationName()}" })

    val options = classifier.options.flatMap { listOf(it.key, it.value) }.toTypedArray()
    val eval = Evaluation(data)
    for (i in 1..10) {
        eval.crossValidateModel(classifier.className, data, 10, options, random)
    }
    val measure = eval.unweightedMacroFmeasure()
    logger.info(Supplier { "end evaluate ${classifier.name} on ${data.relationName()}: $measure" })

    val entity = PerformanceEntity(classifier.name, data.relationName(), measure)
    saveStrategy.save(entity)
}

private fun extractMetaFeatures(config: MetaFeatureConfig) {
    val datasets = config.datasets.map { File(config.datasetFolder, it) }
    val saveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)

    saveStrategy.use { saveStrategy ->
        datasets.parallelStream()
                .forEach { file ->
                    val data = load(file.absolutePath)
                    calculate(data, saveStrategy)
                }
    }
}

private fun calculate(data: Instances, saveStrategy: SaveStrategy) {
    logger.info(Supplier { "start extract meta-features: ${data.relationName()}" })
    val extractor = MetaFeatureExtractor(data)
    logger.info(Supplier { "end extract meta-features: ${data.relationName()}" })

    val result = extractor.extract()
    val entity = MetaFeaturesEntity(data.relationName(), result)
    saveStrategy.save(entity)
}
