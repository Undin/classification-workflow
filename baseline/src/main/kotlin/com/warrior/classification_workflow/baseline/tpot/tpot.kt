package com.warrior.classification_workflow.baseline.tpot

/**
 * Created by warrior on 1/23/17.
 */
fun main(args: Array<String>) {
    val config = readConfig(args, "com.warrior.classification_workflow.baseline.tpot.TpotKt")
    tpotEvaluation(config)
}
