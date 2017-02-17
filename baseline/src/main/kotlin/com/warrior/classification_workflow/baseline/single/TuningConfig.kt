package com.warrior.classification_workflow.baseline.single

import com.fasterxml.jackson.annotation.*
import com.warrior.classification_workflow.core.Classifier

/**
 * Created by warrior on 2/2/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TuningConfig(
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("datasets") val datasets: List<String>,
        @JsonProperty("save_strategy") val saveStrategy: String,
        @JsonProperty("out_folder") val outFolder: String?,
        @JsonProperty("algorithms") val algorithms: List<TuningAlgorithm>,
        @JsonProperty("threads") val threads: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TuningAlgorithm(
        @JsonProperty("classifier") val classifier: Classifier,
        @JsonProperty("params") val params: List<Param>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Param(
        @JsonProperty("name") val name: String,
        @JsonProperty("type") val type: ParamType,
        @JsonProperty("loose_start") val looseStart: String,
        @JsonProperty("loose_end") val looseEnd: String,
        @JsonProperty("loose_step") val looseStep: String,
        @JsonProperty("fine_step") val fineStep: String
)

enum class ParamType {
    EXP,
    LIN;

    fun transform(value: Double): Double = when (this) {
        EXP -> Math.pow(2.0, value)
        LIN -> value
    }

    @JsonValue
    override fun toString(): String = name.toLowerCase()

    companion object {

        @JsonCreator
        @JvmStatic
        fun fromString(value: String): ParamType = valueOf(value.toUpperCase())
    }
}
