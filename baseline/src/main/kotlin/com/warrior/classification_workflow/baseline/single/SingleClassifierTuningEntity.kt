package com.warrior.classification_workflow.baseline.single

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by warrior on 2/2/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleClassifierTuningEntity(
        @JsonProperty("classifier_name") val classifierName: String,
        @JsonProperty("dataset_name") val datasetName: String,
        @JsonProperty("best_params") val bestParams: Map<String, Double>,
        @JsonProperty("score") val score: Double
)
