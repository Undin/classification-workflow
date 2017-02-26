package com.warrior.classification_workflow

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification_workflow.core.ClassifierConfiguration
import com.warrior.classification_workflow.core.TransformerConfiguration
import org.apache.commons.cli.*

/**
 * Created by warrior on 27/08/16.
 */
interface Config {
    val datasetFolder: String
    val threads: Int
    val cachePrefixSize: Int
    val logFolder: String
    val outFolder: String
    val classifiers: List<ClassifierConfiguration>
    val transformers: List<TransformerConfiguration>
    val metaDataPaths: MetaDataPaths
    val params: GeneticAlgorithmParams    
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleDataConfig(
        @JsonProperty("dataset_folder") override val datasetFolder: String,
        @JsonProperty("threads") override val threads: Int,
        @JsonProperty("cache_prefix_size") override val cachePrefixSize: Int,
        @JsonProperty("log_folder") override val logFolder: String,
        @JsonProperty("out_folder") override val outFolder: String,
        @JsonProperty("classifiers") override val classifiers: List<ClassifierConfiguration>,
        @JsonProperty("transformers") override val transformers: List<TransformerConfiguration>,
        @JsonProperty("meta_data_paths") override val metaDataPaths: MetaDataPaths,
        @JsonProperty("params") override val params: GeneticAlgorithmParams,
        @JsonProperty("dataset") val dataset: String
) : Config

@JsonIgnoreProperties(ignoreUnknown = true)
data class EvaluationConfig(
        @JsonProperty("dataset_folder") override val datasetFolder: String,
        @JsonProperty("threads") override val threads: Int,
        @JsonProperty("cache_prefix_size") override val cachePrefixSize: Int,
        @JsonProperty("log_folder") override val logFolder: String,
        @JsonProperty("out_folder") override val outFolder: String,
        @JsonProperty("classifiers") override val classifiers: List<ClassifierConfiguration>,
        @JsonProperty("transformers") override val transformers: List<TransformerConfiguration>,
        @JsonProperty("meta_data_paths") override val metaDataPaths: MetaDataPaths,
        @JsonProperty("params") override val params: GeneticAlgorithmParams,
        @JsonProperty("datasets") val datasets: List<String>
) : Config

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

fun parseArgs(args: Array<String>): String? {
    val configOption = Option.builder("c")
            .longOpt("config")
            .hasArg(true)
            .argName("path")
            .desc("path to config_name.yaml file")
            .build()
    val helpOption = Option.builder("h")
            .longOpt("help")
            .desc("show this help")
            .build()
    val allOptions = options(configOption, helpOption)

    val parser = DefaultParser()
    val line = try {
        parser.parse(options(helpOption), args, false)
    } catch (e: ParseException) {
        null
    }
    if (line != null && line.hasOption(helpOption.opt)) {
        printHelp(allOptions)
        System.exit(0)
    } else {
        try {
            val configOptions = options(configOption)
            val line = parser.parse(configOptions, args)
            if (line.hasOption(configOption.opt)) {
                return line.getOptionValue(configOption.opt)
            } else {
                printHelp(allOptions)
                System.exit(0)
            }
        } catch (e: ParseException) {
            e.printStackTrace(System.err)
            printHelp(allOptions)
            System.exit(1)
        }
    }
    // unreachable
    return ""
}

private fun options(vararg opts: Option): Options {
    val options = Options()
    for (opt in opts) {
        options.addOption(opt)
    }
    return options
}

private fun printHelp(options: Options) {
    val formatter = HelpFormatter()
    formatter.printHelp("java -jar jarfile.jar [options...]", options)
}