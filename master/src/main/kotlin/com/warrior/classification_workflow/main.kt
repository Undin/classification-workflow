package com.warrior.classification_workflow

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import libsvm.svm
import org.apache.commons.cli.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.Supplier
import java.io.File

/**
 * Created by warrior on 29/06/16.
 */
private val logger = LogManager.getLogger("main")

fun main(args: Array<String>) {
    val configPath = parseArgs(args)
    val mapper = jacksonObjectMapper()
    val config: Config = mapper.readValue(File(configPath))

    val ga = GeneticAlgorithm(config, LocalComputationManager(config.threads))
    svm.svm_set_print_string_function { it -> }
    val result = ga.search()
    logger.info(Supplier { result.toString() })
}

private fun parseArgs(args: Array<String>): String? {
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
    val allOptions = options(configOption, helpOption)

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
            val configOptions = options(configOption)
            val line = parser.parse(configOptions, args)
            if (line.hasOption(configOption.opt)) {
                return line.getOptionValue(configOption.opt)
            } else {
                printHelp(allOptions)
                System.exit(0)
            }
        } catch (e: ParseException) {
            logger.error(e.message, e)
            printHelp(allOptions)
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

private fun printHelp(options: Options) {
    val formatter = HelpFormatter()
    formatter.printHelp("java -jar jarfile.jar [options...]", options)
}
