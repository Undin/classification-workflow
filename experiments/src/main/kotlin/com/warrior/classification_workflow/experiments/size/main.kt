package com.warrior.classification_workflow.experiments.size

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.warrior.classification_workflow.WorkflowConstructor
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.experiments.evaluation.readConfig
import weka.core.Instances
import java.io.File

private const val ITERATIONS = 3
private const val MAX_SIZE = 8

fun main(args: Array<String>) {
    val config = readConfig(args)
    val outFolder = File(config.outFolder)
    outFolder.mkdirs()

    val mapper = jacksonObjectMapper()

    for (i in 1 until MAX_SIZE) {
        val size = i + 1
        println("size: $size")
        val params = config.params.copy(maxWorkflowSize = i)
        val iterConfig = config.copy(params = params,
                logFolder = "${config.logFolder}-$size",
                outFolder = "${config.outFolder}-$size")
        val constructor = WorkflowConstructor(iterConfig)
        for (dataset in config.datasets) {
            println(dataset)
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
            var averageScore = 0.0
            for (j in 0 until ITERATIONS) {
                val (_, testScore) = constructor.construct(instances.relationName(), train, test)
                averageScore += testScore
                println("test score for $dataset with size $size: $testScore")
            }
            averageScore /= ITERATIONS
            mapper.writeValue(File(outFolder, "$dataset-$size.json"), mapOf("dataset_name" to dataset, "score" to averageScore))
        }
    }
}
