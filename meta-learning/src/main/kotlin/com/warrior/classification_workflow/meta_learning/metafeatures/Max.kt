package com.warrior.classification_workflow.meta_learning.metafeatures

/**
 * Created by warrior on 11/15/16.
 */
object Max : Aggregator {
    override fun aggregate(values: List<Double>): Double {
        require(values.isNotEmpty()) { "values list must be not empty" }
        return values.max()!!
    }
}
