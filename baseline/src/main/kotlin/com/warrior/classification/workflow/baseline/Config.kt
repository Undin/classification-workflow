package com.warrior.classification.workflow.baseline

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification.workflow.core.AlgorithmConfiguration.ClassifierConfiguration
import com.warrior.classification.workflow.core.Workflow

/**
 * Created by warrior on 17/09/16.
 */
data class Config @JsonCreator constructor(
        @get:JsonProperty("classifiers") @param:JsonProperty("classifiers")val configurations: List<ClassifierConfiguration>?,
        @get:JsonProperty("workflows") @param:JsonProperty("workflows") val workflows: List<Workflow>?,
        @get:JsonProperty("threads") @param:JsonProperty("threads") val threads: Int,
        @get:JsonProperty("save_strategy") @param:JsonProperty("save_strategy") val saveStrategy: String
)
