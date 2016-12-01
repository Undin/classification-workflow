package com.warrior.classification_workflow.core.meta.features

/**
 * Created by warrior on 11/15/16.
 */
interface Aggregator {
    fun aggregate(values: List<Double>): Double
}
