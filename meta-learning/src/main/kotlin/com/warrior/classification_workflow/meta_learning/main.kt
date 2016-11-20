package com.warrior.classification_workflow.meta_learning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.meta_learning.metafeatures.MetaFeatureExtractor
import kotlinx.support.jdk8.collections.parallelStream
import org.apache.commons.cli.*
import weka.core.Instances
import java.io.File

/**
 * Created by warrior on 11/14/16.
 */
fun main(args: Array<String>) {
    val configPath = parseArgs(args)
    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: Config = mapper.readValue(File(configPath))
    extractMetaFeatures(config)
}

private fun parseArgs(args: Array<String>): String {
    val helpOption = Option.builder("h")
            .longOpt("help")
            .desc("show this help")
            .build()
    val configOption = Option.builder("c")
            .longOpt("config")
            .required(true)
            .argName("path")
            .desc("path to config_name.yaml file")
            .build()

    val parser = DefaultParser()
    val line = parser.parse(options(helpOption), args, false)
    if (line.hasOption("h")) {
        printHelp(helpOption, configOption)
        System.exit(0)
    } else {
        try {
            val line = parser.parse(options(configOption), args)
            return line.getOptionValue(configOption.opt)
        } catch (e: ParseException) {
            println(e.message)
            printHelp(helpOption, configOption)
            System.exit(1)
        }
    }
    // unreachable
    return ""
}

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

private fun extractMetaFeatures(config: Config) {
    val datasets = config.datasets.map { File(config.datasetFolder, it) }

    val saveStrategy = when (config.saveStrategy) {
        "json" -> {
            val outFolder = config.outFolder ?: return
            File(outFolder).mkdirs()
            SaveStrategy.JsonSaveStrategy("$outFolder/result-${System.currentTimeMillis()}.json")
        }
        "db" -> SaveStrategy.DatabaseSaveStrategy()
        else -> throw IllegalArgumentException("unknown value for save strategy: ${config.saveStrategy}")
    }

    saveStrategy.use { saveStrategy ->
        datasets.parallelStream()
                .forEach { file ->
                    val data = load(file.absolutePath)
                    calculate(file.nameWithoutExtension, data, saveStrategy)
                }
    }
}

private fun calculate(name: String, data: Instances, saveStrategy: SaveStrategy) {
    println("start: $name")
    val extractor = MetaFeatureExtractor(data)
    val result = extractor.extract()
    val entity = MetaFeaturesEntity(name, result)
    saveStrategy.save(entity)
}
