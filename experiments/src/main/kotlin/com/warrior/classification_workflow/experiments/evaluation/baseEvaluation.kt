package com.warrior.classification_workflow.experiments.evaluation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.EvaluationConfig
import com.warrior.classification_workflow.WorkflowConstructor
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.parseArgs
import weka.core.Instances
import java.io.File

fun evaluation(args: Array<String>, constructorFactory: (EvaluationConfig) -> WorkflowConstructor) {
    val config = readConfig(args)

    val constructor = constructorFactory(config)
    for (dataset in config.datasets) {
        val instances = load("${config.datasetFolder}/$dataset.csv")
        val train = Instances(instances, 0)
        val test = Instances(instances, 0)
        val setNameAttributeIndex = instances.attribute("set_name").index()
        for (instance in instances) {
            if (instance.stringValue(setNameAttributeIndex) == "train") {
                train += instance
            } else {
                test += instance
            }
        }
        train.deleteAttributeAt(setNameAttributeIndex)
        test.deleteAttributeAt(setNameAttributeIndex)
        constructor.construct(instances.relationName(), train, test)
    }
}

fun readConfig(args: Array<String>): EvaluationConfig {
    val configPath = parseArgs(args)
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    return yamlMapper.readValue(File(configPath))
}
