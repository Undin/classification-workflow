package com.warrior.classification_workflow.stacking

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class StakingType {
    WEKA,
    MANUAL;

    @JsonCreator
    fun fromString(value: String): StakingType = StakingType.valueOf(value.toUpperCase())

    @JsonValue
    override fun toString(): String = name.toLowerCase()
}
