package com.warrior.classification_workflow.baseline.single

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.PerformanceEntity

/**
 * Created by warrior on 2/2/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleClassifierTuningEntity(
        @JsonProperty("dataset_name") val datasetName: String,
        @JsonProperty("classifier_name") val classifierName: String,
        @JsonProperty("classifier_class") val className: String,
        @JsonProperty("best_params") val bestParams: Map<String, Double>,
        @JsonProperty("score") val score: Double
) : PerformanceEntity {
    override fun name(): String = datasetName
    override fun score(): Double = score
}
