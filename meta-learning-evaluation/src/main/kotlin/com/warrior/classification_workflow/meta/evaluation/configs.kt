package com.warrior.classification_workflow.meta.evaluation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.Classifier
import com.warrior.classification_workflow.core.Transformer

/**
 * Created by warrior on 11/16/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MetaFeatureConfig(
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("datasets") val datasets: List<String>,
        @JsonProperty("save_strategy") val saveStrategy: String,
        @JsonProperty("out_folder") val outFolder: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClassifierPerfConfig(
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("datasets") val datasets: List<String>,
        @JsonProperty("classifiers") val classifiers: List<Classifier>,
        @JsonProperty("save_strategy") val saveStrategy: String,
        @JsonProperty("out_folder") val outFolder: String?,
        @JsonProperty("current_results") val currentResults: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransformerPerfConfig(
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("datasets") val datasets: List<String>,
        @JsonProperty("classifiers") val classifiers: List<Classifier>,
        @JsonProperty("transformers") val transformers: List<Transformer>,
        @JsonProperty("save_strategy") val saveStrategy: String,
        @JsonProperty("out_folder") val outFolder: String?,
        @JsonProperty("current_results") val currentResults: String?
)
