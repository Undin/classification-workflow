package com.warrior.classification_workflow.stacking

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.PerformanceEntity

@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowStakingPerformanceEntity(
        @JsonProperty("dataset_name") val datasetName: String,
        @JsonProperty("score_test") val testScore: Double
) : PerformanceEntity {
    override fun name(): String = datasetName
    override fun score(): Double = testScore
}
