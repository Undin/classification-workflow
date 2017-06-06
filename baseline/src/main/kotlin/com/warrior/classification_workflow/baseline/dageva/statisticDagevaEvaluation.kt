package com.warrior.classification_workflow.baseline.dageva

fun main(args: Array<String>) {
    val config = readConfig(args, "com.warrior.classification_workflow.baseline.dageva.StatisticDagevaEvaluationKt")
    for (i in 0 until 10) {
        dagevaEvaluation(config)
    }
}
