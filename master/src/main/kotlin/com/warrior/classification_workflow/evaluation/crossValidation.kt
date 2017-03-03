package com.warrior.classification_workflow.evaluation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.WorkflowPerformanceEntity
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.core.parallelCrossValidation
import com.warrior.classification_workflow.core.storage.SaveStrategy
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

/**
 * Created by warrior on 3/3/17.
 */
fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("usage: java -jar config-file.yaml")
        exitProcess(1)
    }

    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: WorkflowCVConfig = yamlMapper.readValue(File(args[0]))

    System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", config.threads.toString())

    val mapper = jacksonObjectMapper()
    val random = Random()
    val logger = LogManager.getLogger("workflow-cv")

    val saveStrategy = SaveStrategy.fromString("json", config.outFolder)
    val needToCalculate = ArrayList<String>()
    saveStrategy.use {
        for (dataset in config.datasets) {
            val resultFile = File(config.resultsFolder, "$dataset.json")
            if (resultFile.exists()) {
                logger.info("start cv in $dataset")
                try {
                    val data = load("${config.datasetFolder}/$dataset.csv")
                    val result: WorkflowPerformanceEntity = mapper.readValue(resultFile)
                    val classifier = result.workflow.classifier()
                    val evaluation = parallelCrossValidation(classifier, "workflow ", data, 10, random, logger)
                    val score = evaluation.unweightedMacroFmeasure()
                    logger.info("cv result on $dataset: $score")
                    val entity = WorkflowCVPerformanceEntity(dataset, result.workflow, score)
                    saveStrategy.save(entity)
                } catch (e: Exception) {
                    logger.error("Error while cv", e)
                    needToCalculate += dataset
                }
            } else {
                needToCalculate += dataset
            }
        }
    }

    logger.info("need to calculate: ${needToCalculate.joinToString("\n")}")
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowCVConfig(
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("datasets") val datasets: List<String>,
        @JsonProperty("results_folder") val resultsFolder: String,
        @JsonProperty("out_folder") val outFolder: String,
        @JsonProperty("threads") val threads: Int
)
