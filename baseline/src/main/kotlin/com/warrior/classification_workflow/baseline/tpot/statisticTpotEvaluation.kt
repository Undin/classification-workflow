package com.warrior.classification_workflow.baseline.tpot

fun main(args: Array<String>) {
    val config = readConfig(args, "com.warrior.classification_workflow.baseline.tpot.StatisticTpotEvaluationKt")
    for (i in 0 until 10) {
        val iterConfig = config.copy(
                outputFolder = "${config.outputFolder}-$i",
                pipelineFolder = "${config.pipelineFolder}-$i"
        )
        tpotEvaluation(iterConfig)
    }
}