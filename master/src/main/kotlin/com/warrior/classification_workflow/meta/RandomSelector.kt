package com.warrior.classification_workflow.meta

import java.util.*

/**
 * Created by warrior on 12/8/16.
 */
class RandomSelector(private val random: Random = Random()) : Selector {

    override fun select(weights: Map<String, Double>): String {
        if (weights.isEmpty()) {
            throw IllegalAccessException("weights must be not empty")
        }
        val list = weights.toList()
        return list[random.nextInt(list.size)].first
    }
}
