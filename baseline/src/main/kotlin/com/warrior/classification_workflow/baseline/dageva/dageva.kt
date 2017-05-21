package com.warrior.classification_workflow.baseline.dageva

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("usage: java -cp baseline.jar com.warrior.classification_workflow.baseline.dageva.DagevaKt config-path.yaml")
        exitProcess(1)
    }

    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: DagevaConfig = yamlMapper.readValue(File(args[0]))

    val configTemplate = File(config.configTemplate).readText()

    val serverProcess = launchServer(config.serverSettings)
    try {
        Thread.sleep(10000)

        for ((i, dataset) in config.datasets.withIndex()) {
            val datasetConfig = createDatasetConfig(configTemplate, config.serverSettings.port,
                    dataset, i == config.datasets.lastIndex)
            val configFile = File.createTempFile("dageva", ".json")
            configFile.writeText(datasetConfig)

            try {
                ProcessBuilder("java", "-jar", config.jar, configFile.absolutePath, config.logFolder)
                        .redirectErrorStream(true)
                        .inheritIO()
                        .start()
                        .waitFor()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                configFile.delete()
            }
        }

    } finally {
        serverProcess.destroy()
    }
}

private fun launchServer(settings: ServerSettings): Process =
        ProcessBuilder(settings.interpreter, "-m", "scoop", "-n",
                settings.workers.toString(), settings.script,
                settings.logFolder, settings.port.toString())
                .inheritIO()
                .redirectErrorStream(true)
                .start()

private fun createDatasetConfig(configTemplate: String, port: Int, datasetName: String, killProcess: Boolean): String =
        configTemplate
                .replace("\$port", port.toString())
                .replace("\$datasetName", datasetName)
                .replace("\$killProcess", killProcess.toString())
