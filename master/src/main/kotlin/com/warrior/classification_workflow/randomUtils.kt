package com.warrior.classification_workflow

import com.warrior.classification_workflow.core.Algorithm
import com.warrior.classification_workflow.core.AlgorithmConfiguration
import java.util.*

/**
 * Created by warrior on 04/09/16.
 */
fun <T> List<T>.randomElement(random: Random): T = get(random.nextInt(size))

fun Map<String, List<String>>.randomValues(random: Random): Map<String, String> {
    val valuesMap = HashMap<String, String>(size)
    for ((key, values) in this) {
        valuesMap[key] = values.randomElement(random)
    }
    return valuesMap
}

fun randomRange(bound: Int, random: Random): IntRange {
    var left = random.nextInt(bound)
    var right = random.nextInt(bound)

    if (left > right) {
        val c = left
        left = right
        right = c
    }
    return IntRange(left, right)
}

fun AlgorithmConfiguration.randomAlgorithm(random: Random): Algorithm = when (this) {
    is AlgorithmConfiguration.ClassifierConfiguration -> randomClassifier(random)
    is AlgorithmConfiguration.TransformerConfiguration -> randomTransformer(random)
}

fun AlgorithmConfiguration.ClassifierConfiguration.randomClassifier(random: Random): Algorithm.Classifier =
        Algorithm.Classifier(name, classifierClass, classifierOptions.randomValues(random))

fun AlgorithmConfiguration.TransformerConfiguration.randomTransformer(random: Random): Algorithm.Transformer =
        Algorithm.Transformer(name, searchClass, searchOptions.randomValues(random),
                evaluationClass, evaluationOptions.randomValues(random))
