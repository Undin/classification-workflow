package com.warrior.classification_workflow.baseline.tpot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.core.storage.SaveStrategy
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.Executors
import kotlin.system.exitProcess

fun tpotEvaluation(config: TpotConfig) {
    val calculatedDatasets = calculatedDatasets(config.currentResults)

    File(config.pipelineFolder).mkdirs()

    val saveStrategy = SaveStrategy.fromString("json", config.outputFolder)
    val threadPool = Executors.newFixedThreadPool(config.threads)

    saveStrategy.use {
        val futures = config.datasets
                .filter { it !in calculatedDatasets }
                .map { dataset ->
                    val datasetName = File(dataset).nameWithoutExtension
                    threadPool.submit {
                        try {
                            println("start: $dataset")
                            val process = launchProcess(config, dataset, datasetName)
                            val entity = performanceEntity(process, datasetName)
                            saveStrategy.save(entity)
                            process.waitFor()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

        for (f in futures) {
            f.get()
        }
        threadPool.shutdown()
    }
}

internal fun readConfig(args: Array<String>, mainClass: String): TpotConfig {
    if (args.size != 1) {
        System.err.println("usage: java -classpath jarfile.jar $mainClass config-file.yaml")
        exitProcess(1)
    }

    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    return yamlMapper.readValue(File(args[0]))
}

private fun calculatedDatasets(results: String): Set<String> {
    val mapper = jacksonObjectMapper()
    return try {
        val results: List<TpotPerformanceEntity> = mapper.readValue(File(results))
        results.map { it.datasetName }.toSet()
    } catch (e: Exception) {
        e.printStackTrace()
        emptySet()
    }
}

private fun launchProcess(config: TpotConfig, dataset: String, datasetName: String): Process {
    return ProcessBuilder("tpot", "${config.datasetFolder}/$dataset",
            "-is", ",",
            "-target", "class",
            "-cv", "10",
            "-g", config.generations.toString(),
            "-p", config.population.toString(),
            "-s", config.randomSeed.toString(),
            "-scoring", "f1_macro",
            "-mode", "classification",
            "-o", "${config.pipelineFolder}/$datasetName.py",
            "-v", "2")
            .redirectErrorStream(true)
            .start()
}

private fun performanceEntity(process: Process, datasetName: String): TpotPerformanceEntity {
    var pipeline: String? = null
    var scoreTrain: Double? = null
    var scoreTest: Double? = null
    InputStreamReader(process.inputStream).forEachLine { line ->
        println(line)
        if (pipeline == null) {
            pipeline = extractResultIfExist(line, "Best pipeline: ")
        }
        if (scoreTrain == null) {
            scoreTrain = extractResultIfExist(line, "Training score: ")?.toDouble()
        }
        if (scoreTest == null) {
            scoreTest = extractResultIfExist(line, "Holdout score: ")?.toDouble()
        }
    }
    return TpotPerformanceEntity(datasetName, pipeline!!, scoreTrain!!, scoreTest!!)
}

private fun extractResultIfExist(line: String, prefix: String): String? =
        if (line.startsWith(prefix)) line.removePrefix(prefix) else null
