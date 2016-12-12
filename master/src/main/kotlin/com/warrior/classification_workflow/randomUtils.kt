package com.warrior.classification_workflow

import com.warrior.classification_workflow.core.*
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
    is ClassifierConfiguration -> randomClassifier(random)
    is TransformerConfiguration -> randomTransformer(random)
    else -> throw UnsupportedOperationException()
}

fun ClassifierConfiguration.randomClassifier(random: Random): Classifier =
        Classifier(name, classifierClass, classifierOptions.randomValues(random))

fun TransformerConfiguration.randomTransformer(random: Random): Transformer
        = Transformer(name, searchConfiguration.randomSearch(random), evaluationConfiguration.randomEvaluation(random))

fun SearchConfiguration.randomSearch(random: Random): Search
        = Search(className, options.randomValues(random))

fun EvaluationConfiguration.randomEvaluation(random: Random): Evaluation
        = Evaluation(className, options.randomValues(random))
