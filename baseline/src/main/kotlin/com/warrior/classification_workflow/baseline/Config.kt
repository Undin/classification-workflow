package com.warrior.classification_workflow.baseline

import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.ClassifierConfiguration
import com.warrior.classification_workflow.core.Workflow

/**
 * Created by warrior on 17/09/16.
 */
data class Config(
        @JsonProperty("classifiers") val configurations: List<ClassifierConfiguration>?,
        @JsonProperty("workflows") val workflows: List<Workflow>?,
        @JsonProperty("threads") val threads: Int,
        @JsonProperty("save_strategy") val saveStrategy: String
)
