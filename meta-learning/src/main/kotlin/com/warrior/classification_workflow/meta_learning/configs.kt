package com.warrior.classification_workflow.meta_learning

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

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

@JsonIgnoreProperties(ignoreUnknown = true)
data class Classifier(
        @JsonProperty("name") val name: String,
        @JsonProperty("classifier_class") val className: String,
        @JsonProperty("classifier_options") val options: Map<String, String>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Transformer(
        @JsonProperty("name") val name: String,
        @JsonProperty("search") val search: Search,
        @JsonProperty("evaluation") val evaluation: Evaluation
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Search(
        @JsonProperty("search_class") val className: String,
        @JsonProperty("search_options") val options: Map<String, String>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Evaluation(
        @JsonProperty("evaluation_class") val className: String,
        @JsonProperty("evaluation_options") val options: Map<String, String>
)
