package com.warrior.classification.workflow.baseline

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification.workflow.core.Workflow

/**
 * Created by warrior on 17/09/16.
 */
data class Config @JsonCreator constructor(
        @get:JsonProperty("workflow") @param:JsonProperty("workflow") val workflow: Workflow,
        @get:JsonProperty("threads") @param:JsonProperty("threads") val threads: Int
)
