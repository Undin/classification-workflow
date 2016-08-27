package com.warrior.classification.workflow.core

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import weka.attributeSelection.ASEvaluation
import weka.attributeSelection.ASSearch
import weka.attributeSelection.AttributeTransformer
import weka.classifiers.AbstractClassifier
import weka.classifiers.Classifier
import weka.core.Instance
import weka.core.Instances
import weka.filters.Filter
import weka.filters.unsupervised.attribute.Remove
import java.util.*

/**
 * Created by warrior on 12/07/16.
 */
class Workflow @JsonCreator constructor(
        @JsonProperty("algorithms") val algorithms: List<Algorithm>,
        @JsonProperty("classifier") val classifier: Algorithm.Classifier
) : AbstractClassifier() {

    val allAlgorithms = algorithms + classifier

    private val builtClassifiers: MutableMap<Int, Classifier> = HashMap()
    private val builtTransformers: MutableMap<Int, Pair<ASSearch, ASEvaluation>> = HashMap()
    private lateinit var builtFinalClassifier: Classifier

    constructor(algorithms: List<Algorithm>) : this(
            ArrayList(algorithms.subList(0, algorithms.lastIndex)),
            algorithms.last() as Algorithm.Classifier
    )

    override fun buildClassifier(data: Instances) {
        val processingData = processInstances(data, true)
        builtFinalClassifier = classifier()
        builtFinalClassifier.buildClassifier(processingData)
    }

    override fun classifyInstance(instance: Instance): Double {
        val currentData = Instances(instance.dataset(), 0)
        currentData.add(instance)
        val processingData = processInstances(currentData, false)
        return builtFinalClassifier.classifyInstance(processingData[0])
    }

    private fun processInstances(data: Instances, needBuildFirst: Boolean): Instances {
        var currentData = data
        for ((i, algo) in algorithms.withIndex()) {
            when (algo) {
                is Algorithm.Classifier -> {
                    val classifier = if (needBuildFirst) {
                        val c = algo()
                        c.buildClassifier(currentData)
                        builtClassifiers[i] = c
                        c
                    } else {
                        builtClassifiers[i]!!
                    }
                    addClassificationsResult(classifier, currentData)
                }
                is Algorithm.Transformer -> {
                    val (search, eval) = if (needBuildFirst) {
                        val searchAndEval = algo()
                        searchAndEval.second.buildEvaluator(currentData)
                        builtTransformers[i] = searchAndEval
                        searchAndEval
                    } else {
                        builtTransformers[i]!!
                    }
                    if (eval is AttributeTransformer) {
                        currentData = eval.transformedData(currentData)
                    } else {
                        val indices = search.search(eval, currentData)
                        currentData = filter(currentData, indices)
                    }
                }
            }
        }
        return currentData
    }

    private fun addClassificationsResult(builtClassifier: Classifier, instances: Instances) {
        val classificationResults = DoubleArray(instances.size)
        for ((i, inst) in instances.withIndex()) {
            classificationResults[i] = builtClassifier.classifyInstance(inst)
        }

        val index = instances.classIndex()
        val attrs = instances.classAttribute().copy("${builtClassifier.javaClass.simpleName}-$index")
        instances.insertAttributeAt(attrs, index)

        for ((i, inst) in instances.withIndex()) {
            inst.setValue(index, classificationResults[i])
        }
    }

    private fun filter(instances: Instances, indices: IntArray): Instances {
        val remove = Remove()
        remove.setAttributeIndicesArray(indices)
        remove.invertSelection = true
        remove.setInputFormat(instances)
        val filteredInstances = Filter.useFilter(instances, remove)
        if (filteredInstances.classIndex() < 0) {
            throw IllegalStateException("filteredInstances.classIndex() < 0")
        }
        return filteredInstances
    }

    override fun toString(): String{
        return "Workflow(" +
                "algorithms=$algorithms," +
                "classifier=$classifier" +
                ")"
    }
}
