package com.warrior.classification_workflow.stacking

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.Result
import com.warrior.classification_workflow.core.*
import libsvm.svm
import org.apache.logging.log4j.LogManager
import weka.classifiers.evaluation.Evaluation
import weka.classifiers.meta.Stacking
import weka.core.Instances
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.system.exitProcess

private val PATTERN = Pattern.compile("^(.*)-(\\d+)$")
private val MAX_INSTANCES = 10000
private val RANDOM = Random()

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("usage: java -classpath jarfile.jar com.warrior.classification_workflow.stacking.Top3Kt config-file.yaml")
        exitProcess(1)
    }
    val logger = LogManager.getLogger("Top3")
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: WorkflowStakingConfig = yamlMapper.readValue(File(args[0]))

    val folders = File(config.workflowResults).listFiles() ?: exitProcess(0)

    val mapper = jacksonObjectMapper()

    val datasetToBestWorkflows = folders.asSequence()
            .filter { File(it, "10.json").exists() }
            .map { file ->
                val matcher = PATTERN.matcher(file.name)
                matcher.find()
                val datasetName = matcher.group(1)
                val time = matcher.group(2).toLong()
                Triple(file, datasetName, time)
            }
            .groupBy({ it.second }) { (file, _, time) -> file to time }
            .mapValues { (_, v) ->
                val folder = v.maxBy { it.second }!!.first
                mapper.readValue<List<Result>>(File(folder, "10.json"))
                        .take(3)
                        .map { it.workflow }
            }

    val outFolder = File(config.outputFolder)
    outFolder.mkdirs()
    svm.svm_set_print_string_function {  }
    for ((datasetName, workflows) in datasetToBestWorkflows) {
        val resultFile = File(outFolder, "$datasetName.json")
        if (resultFile.exists()) {
            logger.info("result for $datasetName exists. skip")
        } else {
            logger.info("start $datasetName")
            val datasetPath = "${config.datasetFolder}/$datasetName.csv"
            try {
                val score = measurePerformance(datasetPath, workflows, config.metaClassifier, config.threads)
                val entity = WorkflowStackingPerformanceEntity(datasetName, config.metaClassifier, score)
                mapper.writeValue(resultFile, entity)
            } catch (e: Exception) {
                logger.error(e.message, e)
            }
        }
    }
}

private fun measurePerformance(datasetPath: String, workflows: List<Workflow>, metaClassifier: Classifier, threads: Int): Double {
    val instances = load(datasetPath).normalize()
    val train = Instances(instances, 0)
    val test = Instances(instances, 0)
    val setNameAttributeIndex = instances.attribute("set_name").index()
    for (instance in instances) {
        if (instance.stringValue(setNameAttributeIndex) == "train") {
            train += instance
        } else {
            test += instance
        }
    }
    train.deleteAttributeAt(setNameAttributeIndex)
    test.deleteAttributeAt(setNameAttributeIndex)

    val subTrain = subInstances(train, MAX_INSTANCES, RANDOM)

    val stacking = Stacking()
    stacking.classifiers = workflows.map { it.classifier() }.toTypedArray()
    stacking.metaClassifier = metaClassifier()
    stacking.numExecutionSlots = threads
    stacking.buildClassifier(subTrain)

    val evaluation = Evaluation(subTrain)
    evaluation.evaluateModel(stacking, test)
    return evaluation.unweightedMacroFmeasure()
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class WorkflowStakingConfig(
        @JsonProperty("meta_classifier") val metaClassifier: Classifier,
        @JsonProperty("threads") val threads: Int,
        @JsonProperty("workflow-results") val workflowResults: String,
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("output_folder") val outputFolder: String
)
