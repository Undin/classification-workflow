package com.warrior.classification.workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.warrior.classification.workflow.Config
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.io.File

/**
 * Created by warrior on 29/06/16.
 */
fun main(args: Array<String>) {
    val arguments = Args()
    val parser = CmdLineParser(arguments)
    try {
        parser.parseArgument(*args)

        val mapper = ObjectMapper()
        val config = mapper.readValue(File(arguments.configPath), Config::class.java)
        val ga = GeneticAlgorithm(config, LocalComputationManager(config.threads))

        libsvm.svm.svm_set_print_string_function { it ->  }
        val result = ga.search()
        println(result)
    } catch (e: CmdLineException) {
        e.printStackTrace(System.err)
        parser.printUsage(System.err)
    }
}

class Args {
    @Option(name = "-c", required = true, usage = "path to config file") lateinit var configPath: String
}
