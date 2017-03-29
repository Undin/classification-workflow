package com.warrior.classification_workflow.baseline.tpot

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.PerformanceEntity

/**
 * Created by warrior on 3/3/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TpotPerformanceEntity(
        @JsonProperty("dataset_name") val datasetName: String,
        @JsonProperty("pipeline") val pipeline: String,
        @JsonProperty("score_train") val trainScore: Double,
        @JsonProperty("score_test") val testScore: Double
) : PerformanceEntity {
    override fun name(): String = datasetName
    override fun score(): Double = testScore
}
