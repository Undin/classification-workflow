package com.warrior.classification_workflow.core.meta.features.informationtheoretic

import weka.attributeSelection.InfoGainAttributeEval
import weka.core.*
import weka.filters.Filter
import weka.filters.supervised.attribute.Discretize
import weka.filters.unsupervised.attribute.Remove

/**
 * Created by warrior on 23.03.15.
 */
val LOG_2 = Math.log(2.0)

fun entropy(values: DoubleArray, numValues: Int): EntropyResult {
    val distribution = DoubleArray(numValues)
    var count = 0
    for (v in values) {
        if (isCorrectValue(v)) {
            distribution[v.toInt()]++
            count++
        }
    }
    for (i in distribution.indices) {
        distribution[i] /= count.toDouble()
    }
    return EntropyResult(ContingencyTables.entropy(distribution), count)
}

fun entropy(instances: Instances, attribute: Attribute): EntropyResult {
    var featureInstances = oneAttributeInstances(instances, attribute)

    if (featureInstances.attribute(0).isNumeric) {
        val discretize = Discretize()
        discretize.useBetterEncoding = true
        discretize.setInputFormat(featureInstances)
        featureInstances = Filter.useFilter(featureInstances, discretize)
    }

    val values = featureInstances.attributeToDoubleArray(0)
    return entropy(values, featureInstances.attribute(0).numValues())
}

fun mutualInformation(instances: Instances, attribute: Attribute): Double {
    val featureInstances = oneAttributeInstances(instances, attribute)
    val infoGain = InfoGainAttributeEval()
    try {
        infoGain.buildEvaluator(featureInstances)
    } catch (e: Exception) {
        // TODO: fix it
        return 0.0
    }
    return infoGain.evaluateAttribute(0)
}

private fun oneAttributeInstances(instances: Instances, attribute: Attribute): Instances {
    val remove = Remove()
    remove.setAttributeIndicesArray(intArrayOf(attribute.index(), instances.classIndex()))
    remove.invertSelection = true
    remove.setInputFormat(instances)
    return Filter.useFilter(instances, remove)
}

private fun isCorrectValue(v: Double): Boolean = !Utils.isMissingValue(v)

class EntropyResult(val entropy: Double, count: Int) {
    val normalizedEntropy: Double = entropy / (Math.log(count.toDouble()) / LOG_2)
}
