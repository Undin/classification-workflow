package com.warrior.classification_workflow.experiments.plot

import com.warrior.classification_workflow.core.Workflow

data class Result(
        val workflow: Workflow,
        val cpuTime: Long
) : Comparable<Result> {
    override fun compareTo(other: Result): Int = cpuTime.compareTo(other.cpuTime)
}
