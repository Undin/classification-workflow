package com.warrior.classification_workflow.baseline.dageva

fun main(args: Array<String>) {
    val config = readConfig(args, "com.warrior.classification_workflow.baseline.dageva.DagevaKt")
    dagevaEvaluation(config)
}
