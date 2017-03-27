package com.warrior.classification_workflow.baseline.single

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by warrior on 3/17/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleClassifierPerformanceEntity(
        @JsonProperty("dataset_name") val datasetName: String,
        @JsonProperty("classifier_name") val classifierName: String,
        @JsonProperty("score_test") val testScore: Double
)