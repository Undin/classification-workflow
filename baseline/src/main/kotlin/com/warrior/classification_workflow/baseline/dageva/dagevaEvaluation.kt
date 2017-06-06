package com.warrior.classification_workflow.baseline.dageva

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import kotlin.system.exitProcess

fun dagevaEvaluation(config: DagevaConfig) {
    val configTemplate = File(config.configTemplate).readText()

    val serverProcess = launchServer(config.serverSettings)
    try {
        Thread.sleep(10000)

        for ((i, dataset) in config.datasets.withIndex()) {
            val datasetConfig = createDatasetConfig(configTemplate, config.serverSettings.port,
                    dataset, i == config.datasets.lastIndex)
            val configFile = File.createTempFile("dageva", ".json")
            configFile.writeText(datasetConfig)

            val datasetLogFolder = File(config.logFolder, dataset)
            datasetLogFolder.mkdirs()
            try {
                ProcessBuilder("java", "-jar", config.jar, configFile.absolutePath, datasetLogFolder.absolutePath)
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

internal fun readConfig(args: Array<String>, mainClass: String): DagevaConfig {
    if (args.size != 1) {
        println("usage: java -cp baseline.jar $mainClass config-path.yaml")
        exitProcess(1)
    }

    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    return yamlMapper.readValue(File(args[0]))
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