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
        private val classifier: Classifier,
        private val random: Random = Random()
) : AbstractClassifier() {

    private val builtClassifiers: MutableMap<Int, weka.classifiers.Classifier> = HashMap()
    private val builtTransformers: MutableMap<Int, AttributeTransformer> = HashMap()
    private val buildIndices: MutableMap<Int, IntArray> = HashMap()
    private lateinit var builtFinalClassifier: weka.classifiers.Classifier

    override fun buildClassifier(data: Instances) {
        val newData = Instances(data)
        newData.randomize(random)
        newData.stratify(NUM_FOLDS)
        val processingData = buildWorkflow(newData)
        builtFinalClassifier = classifier()
        builtFinalClassifier.buildClassifier(processingData)
    }

    override fun distributionForInstance(instance: Instance): DoubleArray = classify(instance).last()

    fun classify(instance: Instance): List<DoubleArray> {
        val distributions = ArrayList<DoubleArray>()
        var currentData = Instances(instance.dataset(), 0)
        currentData.add(instance)
        for ((i, algo) in algorithms.withIndex()) {
            when (algo) {
                is Classifier -> {
                    val classifier = builtClassifiers[i]!!
                    val distribution = classifier.distributionForInstance(currentData[0])
                    val index = insertClassAttribute(currentData, classifier, i)
                    currentData[0].setValue(index, distribution.indexOfMaxValue().toDouble())
                    distributions += distribution
                }
                is Transformer -> {
                    currentData = builtTransformers[i]?.transformedData(currentData) ?: currentData
                    val indices = buildIndices[i]!!
                    currentData = filter(currentData, indices)
                }
            }
        }
        distributions += builtFinalClassifier.distributionForInstance(currentData[0])
        return distributions
    }

    private fun buildWorkflow(data: Instances): Instances {
        var currentData = data
        for ((iteration, algo) in algorithms.withIndex()) {
            currentData = when (algo) {
                is Classifier -> applyClassifier(algo, currentData, iteration)
                is Transformer -> applyTransformer(algo, currentData, iteration)
                else -> throw IllegalStateException("unknown algorithm")
            }
        }
        return currentData
    }

    private fun applyTransformer(algo: Transformer, data: Instances, iteration: Int): Instances {
        var currentData = data
        val (search, eval) = algo()
        if (eval is AttributeTransformer) {
            eval.buildEvaluator(currentData)
            currentData = eval.transformedData(currentData)
            val indices = usefulAttributes(currentData)
            currentData = filter(currentData, indices)
            builtTransformers[iteration] = eval
            buildIndices[iteration] = indices
        } else {
            val attributeSelection = AttributeSelection()
            attributeSelection.setSearch(search)
            attributeSelection.setEvaluator(eval)
            attributeSelection.SelectAttributes(currentData)
            val indices = attributeSelection.selectedAttributes()
            currentData = filter(currentData, indices)
            buildIndices[iteration] = indices
        }
        return currentData
    }

    private fun applyClassifier(algo: Classifier, currentData: Instances, iteration: Int): Instances {
        val classifier = algo()

        // generate values for new attribute
        val classificationResults = DoubleArray(currentData.size)
        var resultIndex = 0
        for (foldIndex in 0 until NUM_FOLDS) {
            val train = currentData.trainCV(NUM_FOLDS, foldIndex)
            val test = currentData.testCV(NUM_FOLDS, foldIndex)
            classifier.buildClassifier(train)
            for (inst in test) {
                classificationResults[resultIndex++] = classifier.classifyInstance(inst)
            }
        }

        // build classifier on all data
        classifier.buildClassifier(currentData)
        builtClassifiers[iteration] = classifier

        // insert new attribute with classifier results
        val index = insertClassAttribute(currentData, classifier, iteration)
        for ((i, inst) in currentData.withIndex()) {
            inst.setValue(index, classificationResults[i])
        }

        return currentData
    }

    private fun insertClassAttribute(instances: Instances, builtClassifier: weka.classifiers.Classifier,
                                     iteration: Int): Int {
        val index = instances.classIndex()
        val attrs = instances.classAttribute().copy("${builtClassifier.javaClass.simpleName}-$iteration")
        instances.insertAttributeAt(attrs, index)
        return index
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

    companion object {
        private const val NUM_FOLDS = 5
    }
}
