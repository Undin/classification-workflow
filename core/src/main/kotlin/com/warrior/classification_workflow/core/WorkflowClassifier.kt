package com.warrior.classification_workflow.core

import weka.attributeSelection.AttributeTransformer
import weka.classifiers.AbstractClassifier
import weka.core.Instance
import weka.core.Instances
import weka.core.Utils
import weka.filters.Filter
import weka.filters.unsupervised.attribute.Remove
import java.util.*

/**
 * Created by warrior on 12/12/16.
 */
internal class WorkflowClassifier(
        private val algorithms: List<Algorithm>,
        private val classifier: Classifier
) : AbstractClassifier() {

    private val builtClassifiers: MutableMap<Int, weka.classifiers.Classifier> = HashMap()
    private val builtTransformers: MutableMap<Int, AttributeTransformer> = HashMap()
    private val buildIndices: MutableMap<Int, IntArray> = HashMap()
    private lateinit var builtFinalClassifier: weka.classifiers.Classifier

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
                is Classifier -> {
                    val classifier = if (needBuildFirst) {
                        val c = algo()
                        c.buildClassifier(currentData)
                        builtClassifiers[i] = c
                        c
                    } else {
                        builtClassifiers[i]!!
                    }
                    addClassificationsResult(classifier, currentData, i)
                }
                is Transformer -> {
                    if (needBuildFirst) {
                        val (search, eval) = algo()
                        eval.buildEvaluator(currentData)
                        if (eval is AttributeTransformer) {
                            currentData = eval.transformedData(currentData)
                            val indices = usefulAttributes(currentData)
                            if (indices.size != currentData.numAttributes()) {
                                currentData = filter(currentData, indices)
                            }

                            builtTransformers[i] = eval
                            buildIndices[i] = indices
                        } else {
                            val indices = search.search(eval, currentData)
                            currentData = filter(currentData, indices)
                            buildIndices[i] = indices
                        }
                    } else {
                        currentData = builtTransformers[i]?.transformedData(currentData) ?: currentData
                        val indices = buildIndices[i]!!
                        if (indices.size != currentData.numAttributes()) {
                            currentData = filter(currentData, indices)
                        }
                    }
                }
            }
        }
        return currentData
    }

    private fun addClassificationsResult(builtClassifier: weka.classifiers.Classifier, instances: Instances, iteration: Int) {
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
