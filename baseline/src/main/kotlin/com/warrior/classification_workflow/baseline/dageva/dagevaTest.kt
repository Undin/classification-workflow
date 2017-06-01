package com.warrior.classification_workflow.baseline.dageva

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("usage: java -cp baseline.jar com.warrior.classification_workflow.baseline.dageva.DagevaTestKt config-path.yaml")
        exitProcess(1)
    }

    val config = readConfig(args[0])
    val outDir = File(config.outDir)
    outDir.mkdirs()

    val mapper = jacksonObjectMapper()
    config.datasets.parallelStream().forEach { dataset ->
        println("[${Thread.currentThread().name}] $dataset")
        val trainResult: DagevaTrainResult = mapper.readValue(File(config.trainResults, "$dataset/result.json"))
        val process = try {
            ProcessBuilder(config.interpreter, config.script,
                    trainResult.json, "$dataset-test.csv")
                    .redirectErrorStream(true)
                    .start()
        } catch (e: Exception) {
            println("can't evaluate $dataset")
            e.printStackTrace()
            null
        }
        if (process != null) {
            try {
                val result = process.inputStream
                        .bufferedReader()
                        .lineSequence()
                        .find { it.startsWith("result: ") }
                if (result != null) {
                    val testScore = result.removePrefix("result: ").toDouble()
                    val entity = DagevaPerformanceEntity(dataset, trainResult.tree, trainResult.fit, testScore)
                    mapper.writeValue(File(outDir, "$dataset.json"), entity)
                } else {
                    println("can't get result for $dataset")
                }
            } catch (e: Exception) {
                println("can't evaluate $dataset")
                e.printStackTrace()
            } finally {
                process.destroy()
            }
        }
    }
}

private fun readConfig(path: String): DagevaTestConfig {
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    return yamlMapper.readValue(File(path))
}
