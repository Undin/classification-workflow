package com.warrior.classification_workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

/**
 * Created by warrior on 2/25/17.
 */
fun main(args: Array<String>) {
    val configPath = parseArgs(args)
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: EvaluationConfig = yamlMapper.readValue(File(configPath))

    val constructor = WorkflowConstructor(config)
    for (datasetName in config.datasets) {
        constructor.construct(datasetName, "$datasetName-train.csv", "$datasetName-test.csv")
    }
}
