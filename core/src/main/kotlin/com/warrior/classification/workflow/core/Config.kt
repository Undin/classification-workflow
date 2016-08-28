package com.warrior.classification.workflow.core

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by warrior on 27/08/16.
 */
data class Config @JsonCreator constructor(
        @JsonProperty("dataset") val dataset: String,
        @JsonProperty("classifiers") val classifiers: List<Algorithm.Classifier>,
        @JsonProperty("transformers") val transformers: List<Algorithm.Transformer>
)
