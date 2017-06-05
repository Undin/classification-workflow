package com.warrior.classification_workflow.experiments.evaluation

import com.warrior.classification_workflow.WorkflowConstructor

fun main(args: Array<String>) {
    for (i in 0 until 10) {
        evaluation(args) { config ->
            val iterConfig = config.copy(logFolder = "${config.logFolder}-$i", version = "${config.version}-$i")
            WorkflowConstructor(iterConfig)
        }
    }
}
