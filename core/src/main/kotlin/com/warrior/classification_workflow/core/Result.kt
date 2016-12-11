package com.warrior.classification_workflow.core

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by warrior on 16/09/16.
 */
data class Result(
        @JsonProperty("workflow") val workflow: Workflow,
        @JsonProperty("measure") val measure: Double
) : Comparable<Result> {
    override operator fun compareTo(other: Result): Int = measure.compareTo(other.measure)
}
