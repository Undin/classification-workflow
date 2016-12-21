package com.warrior.classification_workflow.meta

import java.util.*

/**
 * Created by warrior on 12/21/16.
 */
class SoftMaxSelector(private val random: Random = Random()) : Selector {

    override fun select(weights: Map<String, Double>): String {
        require(weights.isNotEmpty()) { "weights must be not empty" }

        val weightList = weights.toList()
        val sum = weightList.asSequence().map { e -> f(e.second) }.sum()
        val probabilities = ArrayList<Double>(weightList.size)
        var partialSum = 0.0
        for ((name, weight) in weightList) {
            partialSum += f(weight) / sum
            probabilities += partialSum
        }

        val key = random.nextDouble()
        val index = Collections.binarySearch(probabilities, key, Double::compareTo)
        val listIndex = if (index >= 0) { index } else { -index - 1 }
        return weightList[listIndex].first
    }

    private fun f(weight: Double): Double = Math.pow(Math.E, weight)
}
