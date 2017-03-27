package com.warrior.classification_workflow.baseline.single

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.baseline.normalize
import com.warrior.classification_workflow.core.Classifier
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.core.storage.SaveStrategy
import com.warrior.classification_workflow.core.subInstances
import weka.classifiers.Evaluation
import weka.classifiers.functions.LibSVM
import weka.classifiers.trees.RandomForest
import weka.core.Instances
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import kotlin.system.exitProcess

/**
 * Created by warrior on 3/17/17.
 */
private val MAX_INSTANCES = 5000
private val RANDOM = Random()

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("usage: java -classpath jarfile.jar com.warrior.classification_workflow.baseline.single.SingleClassifierPerformanceKt config-file.yaml")
        exitProcess(1)
    }

    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: SingleClassifierPerformanceConfig = yamlMapper.readValue(File(args[0]))

    val currentResults = currentResults(config.currentResults)

    File(config.outputFolder).mkdirs()

    val saveStrategy = SaveStrategy.fromString("json", config.outputFolder)
    val threadPool = Executors.newFixedThreadPool(config.threads)

    val mapper = jacksonObjectMapper()
    saveStrategy.use {
        val futures = config.tuningResults
                .flatMap { mapper.readValue<List<SingleClassifierTuningEntity>>(File(it)) }
                .filter { (classifierName, datasetName) -> Pair(datasetName, classifierName) !in currentResults }
                .map { (classifierName, datasetName, params) ->
                    threadPool.submit {
                        try {
                            println("start $classifierName on $datasetName")
                            val entity = measurePerformance(config.datasetFolder, datasetName, classifierName, params)
                            saveStrategy.save(entity)
                            println("end $classifierName on $datasetName")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

        for (f in futures) {
            f.get()
        }
        threadPool.shutdown()
    }
}

private fun measurePerformance(datasetFolder: String,
                               datasetName: String,
                               classifierName: String,
                               params: Map<String, Double>): SingleClassifierPerformanceEntity {
    val options = params.mapValuesTo(HashMap()) {
        val value = if (classifierName == "SVM") Math.pow(2.0, it.value) else it.value
        // try to use Int for integer params
        val intValue = value.toInt()
        if (intValue.toDouble() == value) intValue.toString() else value.toString()
    }

    val className = when (classifierName) {
        "RF" -> RandomForest::class.qualifiedName!!
        "SVM" -> LibSVM::class.qualifiedName!!
        else -> throw IllegalStateException("unknown classifier name")
    }

    val classifier = Classifier(classifierName, className, options).invoke()

    val instances = load("$datasetFolder/$datasetName.csv").normalize()
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

    classifier.buildClassifier(subTrain)
    val evaluation = Evaluation(subTrain)
    evaluation.evaluateModel(classifier, test)
    val score = evaluation.unweightedMacroFmeasure()
    return SingleClassifierPerformanceEntity(datasetName, classifierName, score)
}

private fun currentResults(path: String?): Set<Pair<String, String>> {
    if (path == null) {
        return emptySet()
    }
    val mapper = jacksonObjectMapper()
    return try {
        val results: List<SingleClassifierPerformanceEntity> = mapper.readValue(File(path))
        results.map { it.datasetName to it.classifierName }.toSet()
    } catch (e: Exception) {
        e.printStackTrace()
        emptySet()
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleClassifierPerformanceConfig(
        @JsonProperty("threads") val threads: Int,
        @JsonProperty("dataset_folder") val datasetFolder: String,
        @JsonProperty("output_folder") val outputFolder: String,
        @JsonProperty("tuning-results") val tuningResults: List<String>,
        @JsonProperty("current_results") val currentResults: String?
)
