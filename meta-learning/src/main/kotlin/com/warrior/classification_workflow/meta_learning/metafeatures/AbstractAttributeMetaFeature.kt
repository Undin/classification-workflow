package com.warrior.classification_workflow.meta_learning.metafeatures

import weka.core.Attribute
import java.util.*

/**
 * Created by warrior on 11/15/16.
 */
abstract class AbstractAttributeMetaFeature(protected val aggregator: Aggregator) : AbstractMetaFeature() {

    protected val attributeMap: MutableMap<Attribute, Double> = HashMap()

    override fun compute(): Double {
        return if (attributeMap.isEmpty()) {
            initialCompute()
        } else {
            incrementalCompute()
        }
    }

    open protected fun initialCompute(): Double = internalCompute()

    private fun incrementalCompute(): Double = internalCompute()

    private fun internalCompute(): Double {
        val values = ArrayList<Double>()
        for (attr in instances.enumerateAttributes()) {
            if (isSuitable(attr)) {
                values += attributeMap.getOrPut(attr) { computeAttributeValue(attr) }
            }
        }
        return aggregator.aggregate(values)
    }

    open protected fun isSuitable(attribute: Attribute): Boolean {
        return isNonClassAttributeWithType(instances, attribute.index(), Attribute.NOMINAL, Attribute.NUMERIC)
    }

    abstract internal fun computeAttributeValue(attribute: Attribute): Double
}
