package com.warrior.classification_workflow.core

import weka.attributeSelection.AttributeSelection
import weka.attributeSelection.AttributeTransformer
import weka.classifiers.AbstractClassifier
import weka.core.Instance
import weka.core.Instances
import weka.core.Utils
import weka.filters.Filter
import weka.filters.unsupervised.attribute.Remove
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by warrior on 12/12/16.
 */
class WorkflowClassifier(
        private val algorithms: List<Algorithm>,
        private val classifier: Classifier
) : AbstractClassifier() {

    private val builtClassifiers: MutableMap<Int, weka.classifiers.Classifier> = HashMap()
    private val builtTransformers: MutableMap<Int, AttributeTransformer> = HashMap()
    private val buildIndices: MutableMap<Int, IntArray> = HashMap()
    private lateinit var builtFinalClassifier: weka.classifiers.Classifier

    override fun buildClassifier(data: Instances) {
        val processingData = buildWorkflow(Instances(data))
        builtFinalClassifier = classifier()
        builtFinalClassifier.buildClassifier(processingData)
    }

    override fun classifyInstance(instance: Instance): Double = classify(instance).last()

    fun classify(instance: Instance): List<Double> {
        val classes = ArrayList<Double>()
        var currentData = Instances(instance.dataset(), 0)
        currentData.add(instance)
        for ((i, algo) in algorithms.withIndex()) {
            when (algo) {
                is Classifier -> {
                    val classifier = builtClassifiers[i]!!
                    classes += addClassificationsResult(classifier, currentData, i)[0]
                }
                is Transformer -> {
                    currentData = builtTransformers[i]?.transformedData(currentData) ?: currentData
                    val indices = buildIndices[i]!!
                    currentData = filter(currentData, indices)
                }
            }
        }
        classes += builtFinalClassifier.classifyInstance(currentData[0])
        return classes
    }

    private fun buildWorkflow(data: Instances): Instances {
        var currentData = data
        for ((i, algo) in algorithms.withIndex()) {
            when (algo) {
                is Classifier -> {
                    val classifier = algo()
                    classifier.buildClassifier(currentData)
                    builtClassifiers[i] = classifier
                    addClassificationsResult(classifier, currentData, i)
                }
                is Transformer -> {
                    val (search, eval) = algo()
                    if (eval is AttributeTransformer) {
                        eval.buildEvaluator(currentData)
                        currentData = eval.transformedData(currentData)
                        val indices = usefulAttributes(currentData)
                        currentData = filter(currentData, indices)
                        builtTransformers[i] = eval
                        buildIndices[i] = indices
                    } else {
                        val attributeSelection = AttributeSelection()
                        attributeSelection.setSearch(search)
                        attributeSelection.setEvaluator(eval)
                        attributeSelection.SelectAttributes(currentData)
                        val indices = attributeSelection.selectedAttributes()
                        currentData = filter(currentData, indices)
                        buildIndices[i] = indices
                    }
                }
            }
        }
        return currentData
    }

    private fun addClassificationsResult(builtClassifier: weka.classifiers.Classifier,
                                         instances: Instances, iteration: Int): DoubleArray {
        val classificationResults = DoubleArray(instances.size)
        for ((i, inst) in instances.withIndex()) {
            classificationResults[i] = builtClassifier.classifyInstance(inst)
        }

        val index = instances.classIndex()
        val attrs = instances.classAttribute().copy("${builtClassifier.javaClass.simpleName}-$iteration")
        instances.insertAttributeAt(attrs, index)

        for ((i, inst) in instances.withIndex()) {
            inst.setValue(index, classificationResults[i])
        }
        return classificationResults
    }

    private fun filter(instances: Instances, indices: IntArray): Instances {
        var finalIndices = indices
        if (!finalIndices.contains(instances.classIndex())) {
            val newIndices = IntArray(indices.size + 1)
            System.arraycopy(indices, 0, newIndices, 0, indices.size)
            newIndices[newIndices.lastIndex] = instances.classIndex()
            finalIndices = newIndices
        }
        val remove = Remove()
        remove.setAttributeIndicesArray(finalIndices)
        remove.invertSelection = true
        remove.setInputFormat(instances)
        val filteredInstances = Filter.useFilter(instances, remove)
        if (filteredInstances.classIndex() < 0) {
            throw IllegalStateException("filteredInstances.classIndex() < 0")
        }
        return filteredInstances
    }

    private fun usefulAttributes(instances: Instances): IntArray {
        val usefulAttributes = ArrayList<Int>(instances.numAttributes())
        for (i in 0 until instances.numAttributes()) {
            val attr = instances.attribute(i)
            if (!attr.isNumeric) {
                usefulAttributes += i
            } else {
                var numValues = 0
                var prevValue = Double.NaN
                for (inst in instances) {
                    val value = inst.value(i)
                    if (!Utils.isMissingValue(value) && value != prevValue) {
                        prevValue = value
                        numValues++
                    }
                    if (numValues > 1) {
                        usefulAttributes += i
                        break
                    }
                }
            }
        }
        return usefulAttributes.toIntArray()
    }
}
