package com.warrior.classification_workflow

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.PerformanceEntity
import com.warrior.classification_workflow.core.Workflow

/**
 * Created by warrior on 1/24/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowPerformanceEntity(
        @JsonProperty("dataset_name") val datasetName: String,
        @JsonProperty("workflow") val workflow: Workflow,
        @JsonProperty("score_train") val trainScore: Double,
        @JsonProperty("score_test") val testScore: Double,
        @JsonProperty("version") val version: String = "v1"
) : PerformanceEntity {
    override fun name(): String = datasetName
    override fun score(): Double = testScore
}
