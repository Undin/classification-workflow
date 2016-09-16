package com.warrior.classification.workflow.core

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by warrior on 16/09/16.
 */
data class Result @JsonCreator constructor(
        @get:com.fasterxml.jackson.annotation.JsonProperty("workflow") @param:JsonProperty("workflow") val workflow: Workflow,
        @get:com.fasterxml.jackson.annotation.JsonProperty("measure") @param:JsonProperty("workflow") val measure: Double
) : Comparable<Result> {
    override operator fun compareTo(other: Result): Int = measure.compareTo(other.measure)
}
