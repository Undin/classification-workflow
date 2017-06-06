package com.warrior.classification_workflow.baseline.tpot

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TpotConfig(
        @JsonProperty("threads") val threads: Int,
        @JsonProperty("generations") val generations: Int,
        @JsonProperty("population") val population: Int,
        @JsonProperty("random_seed") val randomSeed: Int,
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("output_folder") val outputFolder: String,
        @JsonProperty("pipeline_folder") val pipelineFolder: String,
        @JsonProperty("datasets") val datasets: List<String>,
        @JsonProperty("current_results") val currentResults: String
)
