package com.warrior.classification_workflow.baseline.dageva

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DagevaTrainResult(
        @JsonProperty("fit") val fit: Double,
        @JsonProperty("tree") val tree: String,
        @JsonProperty("json") val json: String
)
