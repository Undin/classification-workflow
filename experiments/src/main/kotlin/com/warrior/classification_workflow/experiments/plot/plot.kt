package com.warrior.classification_workflow.experiments.plot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.warrior.classification_workflow.*
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.core.meta.entity.ClassifierPerformanceEntity
import com.warrior.classification_workflow.core.meta.entity.MetaFeaturesEntity
import com.warrior.classification_workflow.core.meta.entity.TransformerPerformanceEntity
import com.warrior.classification_workflow.meta.AlgorithmChooser
import libsvm.svm
import weka.classifiers.Evaluation
import weka.core.Instances
import java.io.File
import java.util.HashSet
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("usage: java -cp jarfile.jar com.warrior.classification_workflow.cpu.PlotKt plot-config.yaml")
        exitProcess(1)
    }
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: SingleDataConfig = yamlMapper.readValue(File(args[0]))

    val (train, test) = loadData(config)

    for (i in 1..5) {

        val algorithmChooser = algorithmChooser(config, config.dataset)
        val output = ArrayList<Result>()
        val computationManager = computationManager(config, algorithmChooser, train, output)
        val ga = GeneticAlgorithm(config, computationManager, config.dataset)
        svm.svm_set_print_string_function { }

        ga.search(config.dataset)
        val (cpuTimes, scores) = evaluate(output, train, test)
        val resultFolder = File(config.outFolder, config.dataset)
        resultFolder.mkdirs()

        File(resultFolder, "$i.txt").printWriter().use { writer ->
            writer.println((listOf(0L) + cpuTimes).joinToString("\t"))
            writer.println((listOf(0.0) + scores).joinToString("\t"))
        }
    }

}

private fun evaluate(results: List<Result>, train: Instances, test: Instances): Pair<List<Long>, List<Double>> {
    val sortedResults = results.sorted()

    var maxScore = 0.0
    val cpuTimes = ArrayList<Long>()
    val scores = ArrayList<Double>()
    for ((workflow, cpuTime) in sortedResults) {
        val c = workflow.classifier()
        try {
            c.buildClassifier(train)
            val eval = Evaluation(train)
            eval.evaluateModel(c, test)
            val score = eval.unweightedMacroFmeasure()
            if (score > maxScore) {
                cpuTimes += cpuTime / 1000000
                scores += score
                maxScore = score
                println("cpuTime: $cpuTime, score: $score")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return cpuTimes to scores
}

private fun loadData(config: SingleDataConfig): Pair<Instances, Instances> {
    val instances = load("${config.datasetFolder}/${config.dataset}.csv")
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
    return train to test
}


private fun algorithmChooser(config: Config, datasetName: String): AlgorithmChooser {
    val jsonMapper = jacksonObjectMapper()
    val paths = config.metaDataPaths
    val metaFeatures: List<MetaFeaturesEntity> = jsonMapper.readValue(File(paths.metaFeaturesPath))
    val classifierPerformance: List<ClassifierPerformanceEntity> = jsonMapper.readValue(File(paths.classifierPerformancePath))
    val transformerPerformance: List<TransformerPerformanceEntity> = jsonMapper.readValue(File(paths.transformerPerformancePath))

    val datasets = metaFeatures.mapTo(HashSet()) { it.datasetName }
    datasets.remove(datasetName)

    val classifiers = classifierPerformance.mapTo(HashSet()) { it.classifierName }
    val transformers = transformerPerformance.mapTo(HashSet()) { it.transformerName }

    val algorithmChooser = AlgorithmChooser(
            datasets = datasets,
            classifiers = classifiers,
            transformers = transformers,
            metaFeatures = metaFeatures,
            classifierPerformance = classifierPerformance,
            transformerPerformance = transformerPerformance
    )
    return algorithmChooser
}

private fun computationManager(config: Config, algorithmChooser: AlgorithmChooser, instances: Instances, output: MutableList<Result>): LocalComputationManager {
    val classifierMap = config.classifiers.associateBy { it.name }
    val transformerMap = config.transformers.associateBy { it.name }
    val cache: Cache<String, MutableMap<Int, Instances>> = Caffeine.newBuilder()
            .maximumSize((config.params.populationSize + 1) * config.params.mutationNumber.toLong())
            .build()

    val computationManager = CPUTimeComputationManager(
            output = output,
            instances = instances,
            algorithmChooser = algorithmChooser,
            classifiersMap = classifierMap,
            transformersMap = transformerMap,
            cache = cache,
            cachePrefixSize = config.cachePrefixSize,
            threads = config.threads
    )
    return computationManager
}
