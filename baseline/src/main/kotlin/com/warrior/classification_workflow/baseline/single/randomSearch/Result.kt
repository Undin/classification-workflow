package com.warrior.classification_workflow.baseline.single.randomSearch

import weka.core.setupgenerator.Point

data class Result(
        val cpuTime: Long,
        val values: Point<Any>
) : Comparable<Result> {
    override fun compareTo(other: Result): Int = cpuTime.compareTo(other.cpuTime)
}
