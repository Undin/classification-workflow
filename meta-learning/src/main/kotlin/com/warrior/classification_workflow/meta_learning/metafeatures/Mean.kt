package com.warrior.classification_workflow.meta_learning.metafeatures

/**
 * Created by warrior on 11/15/16.
 */
object Mean : Aggregator {
    override fun aggregate(values: List<Double>): Double = values.average()
}
