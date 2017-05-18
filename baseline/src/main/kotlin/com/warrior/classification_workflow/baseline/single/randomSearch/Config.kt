package com.warrior.classification_workflow.baseline.single.randomSearch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Config(
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("dataset") val dataset: String,
        @JsonProperty("out_folder") val outFolder: String,
        @JsonProperty("max_cpu_time") val maxCPUTime: Long
)
