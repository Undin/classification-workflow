package com.warrior.classification_workflow.baseline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.core.meta.entity.ClassifierPerformanceEntity
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.Executors

/**
 * Created by warrior on 1/23/17.
 */
private val mapper = jacksonObjectMapper()

fun main(args: Array<String>) {
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: TpotConfig = yamlMapper.readValue(File("tpot-config.yaml"))

    val trainResults = File("results/tpot-performance/train")
    trainResults.mkdirs()
    val testResults = File("results/tpot-performance/test")
    testResults.mkdirs()

    val threadPool = Executors.newFixedThreadPool(config.threads)
    val futures = config.datasets.map { dataset ->
        val datasetName = File(dataset).nameWithoutExtension
        threadPool.submit {
            try {
                println("start: $dataset")
                val process = ProcessBuilder("tpot", "${config.folder}/$dataset",
                        "-is", ";",
                        "-target", "class",
                        "-cv", "10",
                        "-g", config.generations.toString(),
                        "-p", config.population.toString(),
                        "-scoring", "f1_macro",
                        "-mode", "classification",
                        "-v", "2")
                        .redirectErrorStream(true)
                        .start()
                InputStreamReader(process.inputStream).forEachLine { line ->
                    println(line)
                    saveResultInNeed(line, "Training score: ", datasetName, trainResults)
                    saveResultInNeed(line, "Holdout score: ", datasetName, testResults)
                }

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

private fun saveResultInNeed(line: String, prefix: String, datasetName: String, folder: File) {
    if (line.startsWith(prefix)) {
        val score = line.removePrefix(prefix).toDouble()
        val result = ClassifierPerformanceEntity(datasetName, "tpot", score)
        mapper.writeValue(File(folder, "tpot-$datasetName.json"), result)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TpotConfig(
        @JsonProperty("threads") val threads: Int,
        @JsonProperty("generations") val generations: Int,
        @JsonProperty("population") val population: Int,
        @JsonProperty("folder") val folder: String,
        @JsonProperty("datasets") val datasets: List<String>
)
