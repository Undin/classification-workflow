package com.warrior.classification_workflow.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by warrior on 12/13/16.
 */
interface AlgorithmConfiguration

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClassifierConfiguration(
        @JsonProperty("name") val name: String,
        @JsonProperty("classifier_class") val classifierClass: String,
        @JsonProperty("classifier_options") val classifierOptions: Map<String, List<String>>
) : AlgorithmConfiguration

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransformerConfiguration(
        @JsonProperty("name") val name: String,
        @JsonProperty("search") val searchConfiguration: SearchConfiguration,
        @JsonProperty("evaluation") val evaluationConfiguration: EvaluationConfiguration
) : AlgorithmConfiguration

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchConfiguration(
        @JsonProperty("search_class") val className: String,
        @JsonProperty("search_options") val options: Map<String, List<String>>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EvaluationConfiguration(
        @JsonProperty("evaluation_class") val className: String,
        @JsonProperty("evaluation_options") val options: Map<String, List<String>>
)
