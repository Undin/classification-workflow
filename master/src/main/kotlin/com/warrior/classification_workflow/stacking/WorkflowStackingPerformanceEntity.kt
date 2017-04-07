package com.warrior.classification_workflow.stacking

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.Classifier
import com.warrior.classification_workflow.core.PerformanceEntity

@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowStackingPerformanceEntity(
        @JsonProperty("dataset_name") val datasetName: String,
        @JsonProperty("meta_classifier") val metaClassifier: Classifier,
        @JsonProperty("score_test") val testScore: Double,
        @JsonProperty("stacking_type") val stackingType: StakingType
) : PerformanceEntity {
    override fun name(): String = datasetName
    override fun score(): Double = testScore
}
