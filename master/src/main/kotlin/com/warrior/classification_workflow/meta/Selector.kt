package com.warrior.classification_workflow.meta

/**
 * Created by warrior on 12/8/16.
 */
interface Selector {
    fun select(weights: Map<String, Double>): String
}
