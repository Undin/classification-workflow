package com.warrior.classification_workflow.core

import weka.core.Instances
import weka.core.converters.*
import weka.filters.Filter
import weka.filters.unsupervised.attribute.Remove
import java.io.File
import java.util.*

/**
 * Created by warrior on 12/07/16.
 */
fun load(path: String, removeUseless: Boolean = true): Instances {
    val dataFile = File(path)
    val loader = when (dataFile.extension) {
        "arff" -> arffLoader()
        "csv" -> csvLoader()
        else -> throw IllegalArgumentException("unsupported extension: ${dataFile.extension}")
    }
    loader.setFile(dataFile)


    val instances = ConverterUtils.DataSource.read(loader)
    instances.setClassIndex(instances.numAttributes() - 1)
    return if (removeUseless) {
        val filteredInstances = removeUseless(instances)
        filteredInstances.setRelationName(instances.relationName())
        filteredInstances
    } else {
        instances
    }
}

private fun csvLoader(): AbstractFileLoader {
    val loader = CSVLoader()
    loader.nominalAttributes = "last"
    return loader
}

private fun arffLoader(): AbstractFileLoader = ArffLoader()

private fun removeUseless(instances: Instances): Instances {
    val firstInstance = instances[0]
    val uselessAttributes = ArrayList<Int>()
    for (attr in instances.enumerateAttributes()) {
        if (attr.index() != instances.classIndex()) {
            val isConstAttr = instances.none { it.value(attr) != firstInstance.value(attr) }
            if (isConstAttr) {
                uselessAttributes += attr.index()
            }
        }
    }
    return if (uselessAttributes.isNotEmpty()) {
        val remove = Remove()
        remove.setAttributeIndicesArray(uselessAttributes.toIntArray())
        remove.setInputFormat(instances)
        Filter.useFilter(instances, remove)
    } else {
        instances
    }
}

fun save(dataSet: Instances, dst: String, saver: Saver = ArffSaver()) {
    saver.setInstances(dataSet)
    saver.setFile(File(dst))
    saver.writeBatch()
}
