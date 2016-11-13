package com.warrior.classification_workflow

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.AlgorithmConfiguration.ClassifierConfiguration
import com.warrior.classification_workflow.core.AlgorithmConfiguration.TransformerConfiguration

/**
 * Created by warrior on 27/08/16.
 */
data class Config @JsonCreator constructor(
        @JsonProperty("dataset") val dataset: String,
        @JsonProperty("classifiers") val classifiers: List<ClassifierConfiguration>,
        @JsonProperty("transformers") val transformers: List<TransformerConfiguration>,
        @JsonProperty("threads") val threads: Int,
        @JsonProperty("population_size") val populationSize: Int,
        @JsonProperty("generations") val generations: Int,
        @JsonProperty("max_workflow_size") val maxWorkflowSize: Int,
        @JsonProperty("survived_part") val survivedPart: Double,
        @JsonProperty("mutation_probability") val mutationProbability: Double,
        @JsonProperty("tournament_probability") val tournamentProbability: Double,
        @JsonProperty("point_crossover_probability") val pointCrossoverProbability: Double,
        @JsonProperty("structure_mutation_probability") val structureMutationProbability: Double,
        @JsonProperty("log_folder") val logFolder: String
)
