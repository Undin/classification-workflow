package com.warrior.classification_workflow

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.ClassifierConfiguration
import com.warrior.classification_workflow.core.TransformerConfiguration

/**
 * Created by warrior on 27/08/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Config(
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("dataset") val dataset: String,
        @JsonProperty("threads") val threads: Int,
        @JsonProperty("log_folder") val logFolder: String,
        @JsonProperty("classifiers") val classifiers: List<ClassifierConfiguration>,
        @JsonProperty("transformers") val transformers: List<TransformerConfiguration>,
        @JsonProperty("meta_data_paths") val metaDataPaths: MetaDataPaths,
        @JsonProperty("params") val params: GeneticAlgorithmParams
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MetaDataPaths(
        @JsonProperty("meta_features_path") val metaFeaturesPath: String,
        @JsonProperty("classifier_performance_path") val classifierPerformancePath: String,
        @JsonProperty("transformer_performances_path") val transformerPerformancePath: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeneticAlgorithmParams(
        @JsonProperty("population_size") val populationSize: Int,
        @JsonProperty("generations") val generations: Int,
        @JsonProperty("max_workflow_size") val maxWorkflowSize: Int,
        @JsonProperty("mutation_number") val mutationNumber: Int,
        @JsonProperty("survived_part") val survivedPart: Double,
        @JsonProperty("mutation_probability") val mutationProbability: Double,
        @JsonProperty("tournament_probability") val tournamentProbability: Double,
        @JsonProperty("point_crossover_probability") val pointCrossoverProbability: Double,
        @JsonProperty("structure_mutation_probability") val structureMutationProbability: Double
)
