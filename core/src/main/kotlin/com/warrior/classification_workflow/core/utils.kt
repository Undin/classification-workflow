package com.warrior.classification_workflow.core

import kotlinx.support.jdk8.collections.parallelStream
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.util.Supplier
import weka.classifiers.AbstractClassifier
import weka.classifiers.AggregateableEvaluation
import weka.classifiers.Evaluation
import weka.core.*
import weka.core.converters.*
import weka.filters.Filter
import weka.filters.unsupervised.attribute.NumericToNominal
import weka.filters.unsupervised.attribute.Remove
import java.io.File
import java.util.*
import java.util.stream.IntStream

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


    var instances = ConverterUtils.DataSource.read(loader)
    instances.setClassIndex(instances.numAttributes() - 1)
    if (dataFile.extension == "csv") {
        instances = numericToNominal(instances)
    }

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

private fun numericToNominal(data: Instances): Instances {
    val indices = ArrayList<Int>()
    for (attr in data.enumerateAttributes()) {
        if (isNominal(attr, data)) {
            indices += attr.index() + 1
        }
    }

    val filter = NumericToNominal()
    filter.attributeIndices = indices.joinToString(transform = Int::toString)
    filter.setInputFormat(data)
    return Filter.useFilter(data, filter).apply { setRelationName(data.relationName()) }
}

private fun isNominal(attribute: Attribute, data: Instances): Boolean {
    if (attribute.isNominal) {
        return true
    }
    if (!attribute.isNumeric) {
        return false
    }

    val values = HashSet<Int>()
    for (instance in data) {
        val value = instance.value(attribute)
        val intValue = value.toInt()
        if (value == intValue.toDouble()) {
            values += intValue
        } else {
            return false
        }
    }
    return values.size <= 10
}

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

fun subInstances(data: Instances, maxInstances: Int, random: Random): Instances {
    if (data.numInstances() <= maxInstances) {
        return data
    }

    val instancesByClass = Array(data.numClasses()) { ArrayList<Instance>() }

    for (instance in data) {
        val classValue = instance.classValue()
        if (!Utils.isMissingValue(classValue)) {
            instancesByClass[classValue.toInt()].add(instance)
        }
    }
    val sum = instancesByClass.sumBy { it.size }.toDouble()
    val distribution = DoubleArray(data.numClasses()) { i -> instancesByClass[i].size / sum }

    val subInstances = Instances(data, 0)
    for ((i, list) in instancesByClass.withIndex()) {
        val number = Math.min((distribution[i] * maxInstances).toInt(), list.size)
        Collections.shuffle(list, random)
        subInstances += list.take(number)
    }
    return subInstances
}

inline fun <T> Logger?.withLog(message: String, block: () -> T): T {
    this?.info(Supplier { "start $message" })
    val result = block()
    this?.info(Supplier { "end $message" })
    return result
}

fun Map<String, String>.toArray(): Array<String>
        = flatMap { listOf(it.key, it.value) }.toTypedArray()

fun <T> Collection<T>.forEachParallel(action: (T) -> Unit)
        = parallelStream().forEach { action(it) }

fun parallelCrossValidation(classifier: Classifier, data: Instances, folds: Int, random: Random, logger: Logger? = null): Evaluation {
    val options = classifier.options.toArray()
    val wekaClassifier = AbstractClassifier.forName(classifier.className, options)

    val shuffledData = Instances(data)
    shuffledData.randomize(random)

    val fullAggregation: AggregateableEvaluation = IntStream.range(0, folds)
            .parallel()
            .mapToObj { fold ->
                logger.withLog("${classifier.name} on ${data.relationName()}: fold $fold") {
                    val train = shuffledData.trainCV(folds, fold, random)
                    val copiedClassifier = AbstractClassifier.makeCopy(wekaClassifier)
                    copiedClassifier.buildClassifier(train)
                    val test = shuffledData.testCV(folds, fold)
                    val eval = Evaluation(shuffledData)
                    eval.evaluateModel(copiedClassifier, test)
                    eval
                }
            }.collect(
            { AggregateableEvaluation(data) },
            { acc, o -> acc.aggregate(o) },
            { l, r -> l.aggregate(r) }
    )
    return fullAggregation
}
