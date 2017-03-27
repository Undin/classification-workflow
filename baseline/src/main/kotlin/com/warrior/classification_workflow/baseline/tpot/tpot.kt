package com.warrior.classification_workflow.baseline.tpot

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
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

/**
 * Created by warrior on 1/23/17.
 */
fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("usage: java -classpath jarfile.jar com.warrior.classification_workflow.baseline.tpot.TpotKt config-file.yaml")
        exitProcess(1)
    }

    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: TpotConfig = yamlMapper.readValue(File(args[0]))
    val calculatedDatasets = calculatedDatasets(config.currentResults)

    File(config.pipelineFolder).mkdirs()

    val saveStrategy = SaveStrategy.fromString("json", "results/tpot")
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

private fun extractResultIfExist(line: String, prefix: String): String? {
    return if (line.startsWith(prefix)) line.removePrefix(prefix) else null
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TpotConfig(
        @JsonProperty("threads") val threads: Int,
        @JsonProperty("generations") val generations: Int,
        @JsonProperty("population") val population: Int,
        @JsonProperty("random_seed") val randomSeed: Int,
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("output_folder") val outputFolder: String,
        @JsonProperty("pipeline_folder") val pipelineFolder: String,
        @JsonProperty("datasets") val datasets: List<String>,
        @JsonProperty("current_results") val currentResults: String
)
