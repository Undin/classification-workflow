package com.warrior.classification_workflow.baseline.single.randomSearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.baseline.single.randomSearch.ext.MultiSearchExt
import com.warrior.classification_workflow.core.load
import libsvm.svm
import weka.classifiers.AbstractClassifier
import weka.classifiers.Evaluation
import weka.classifiers.functions.LibSVM
import weka.classifiers.trees.RandomForest
import weka.core.Instances
import weka.core.SetupGenerator
import weka.core.setupgenerator.AbstractParameter
import weka.core.setupgenerator.MathParameter
import java.io.File
import java.io.PrintWriter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("usage: java -cp jarfile.jar com.warrior.classification_workflow.baseline.single.randomSearch.RandomSearchKt random-search-config.yaml")
        exitProcess(1)
    }

    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: Config = yamlMapper.readValue(File(args[0]))

    val (train, test) = loadData(config)
    svm.svm_set_print_string_function {  }

    for (i in 1..5) {
        val rfFolder = File("${config.outFolder}/${config.dataset}/rf")
        rfFolder.mkdirs()

        val randomForestResult = evaluate(train, test, config.maxCPUTime * 1000000) { randomForestParams(train) }
        saveResult(randomForestResult, File(rfFolder, "$i.txt"))

        val svmFolder = File("${config.outFolder}/${config.dataset}/svm")
        svmFolder.mkdirs()
        val svmResults = evaluate(train, test, config.maxCPUTime * 1000000, ::svmParams)
        saveResult(svmResults, File(svmFolder, "$i.txt"))
    }
}

private fun saveResult(results: Pair<List<Long>, List<Double>>, out: File) {
    PrintWriter(out).use { writer ->
        writer.println((listOf(0L) + results.first).joinToString("\t"))
        writer.println((listOf(0.0) + results.second).joinToString("\t"))
    }
}

private fun evaluate(train: Instances, test: Instances,
                     maxCPUTime: Long,
                     factory: () -> Pair<AbstractClassifier, Array<AbstractParameter>>): Pair<List<Long>, List<Double>> {
    val (classifier, params) = factory()
    val results = ArrayList<Result>()
    MultiSearchExt.init(results, maxCPUTime)
    val multiSearch = MultiSearchExt()
    multiSearch.classifier = classifier
    multiSearch.searchParameters = params
    multiSearch.buildClassifier(train)

    results.sort()
    val setupGenerator = SetupGenerator()
    setupGenerator.parameters = params

    var maxScore = 0.0
    val cpuTimes = ArrayList<Long>()
    val scores = ArrayList<Double>()
    for ((cpuTime, values) in results) {
        val evaluatedValues = setupGenerator.evaluate(values)
        val c = setupGenerator.setup(classifier, evaluatedValues) as AbstractClassifier
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
    }

    return cpuTimes to scores
}

private fun loadData(config: Config): Pair<Instances, Instances> {
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

private fun svmParams(): Pair<AbstractClassifier, Array<AbstractParameter>> {
    val gammaParameter = MathParameter()
    gammaParameter.property = "gamma"
    gammaParameter.min = -15.0
    gammaParameter.max = 3.0
    gammaParameter.step = 2.0
    gammaParameter.base = 2.0

    val costParameter = MathParameter()
    costParameter.property = "cost"
    costParameter.min = -5.0
    costParameter.max = 15.0
    costParameter.step = 2.0
    costParameter.base = 2.0

    return LibSVM() to arrayOf<AbstractParameter>(gammaParameter, costParameter)
}

private fun randomForestParams(data: Instances): Pair<AbstractClassifier, Array<AbstractParameter>> {
    val numIterationsParameter = MathParameter()
    numIterationsParameter.property = "numIterations"
    numIterationsParameter.expression = "I"
    numIterationsParameter.min = 10.0
    numIterationsParameter.max = 300.0
    numIterationsParameter.step = 5.0

    val numFeaturesParameter = MathParameter()
    numFeaturesParameter.property = "numFeatures"
    numFeaturesParameter.expression = "I"
    numFeaturesParameter.min = 1.0
    val max = Math.round(2 * Math.sqrt(data.numAttributes().toDouble()))
    val step = Math.max(1, (max - 1) / 10)
    numFeaturesParameter.step = step.toDouble()
    numFeaturesParameter.max = if (step == 1L) Math.min(10, max - 1).toDouble() else ((max - 1) / step).toDouble()

    return RandomForest() to arrayOf<AbstractParameter>(numIterationsParameter, numFeaturesParameter)
}