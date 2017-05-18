package com.warrior.classification_workflow.experiments.evaluation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.PerformanceEntity
import com.warrior.classification_workflow.core.Workflow

/**
 * Created by warrior on 3/3/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowCVPerformanceEntity(
        @JsonProperty("dataset_name") val datasetName: String,
        @JsonProperty("workflow") val workflow: Workflow,
        @JsonProperty("score") val score: Double
) : PerformanceEntity {
    override fun name(): String = datasetName
    override fun score(): Double = score
}
