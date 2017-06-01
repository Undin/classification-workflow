package com.warrior.classification_workflow.baseline.dageva

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DagevaTestConfig(
        @JsonProperty("interpreter") val interpreter: String,
        @JsonProperty("script") val script: String,
        @JsonProperty("datasets") val datasets: List<String>,
        @JsonProperty("train_results") val trainResults: String,
        @JsonProperty("out_dir") val outDir: String
)
