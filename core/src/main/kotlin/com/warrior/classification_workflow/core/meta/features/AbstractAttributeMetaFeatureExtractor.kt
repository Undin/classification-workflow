package com.warrior.classification_workflow.core.meta.features

import weka.core.Attribute
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Created by warrior on 11/15/16.
 */
abstract class AbstractAttributeMetaFeatureExtractor(protected val aggregator: Aggregator) : AbstractMetaFeatureExtractor() {

    protected val attributeMap: ConcurrentMap<Attribute, Double> = ConcurrentHashMap()

    override fun compute(): Double {
        return if (attributeMap.isEmpty()) {
            initialCompute()
        } else {
            incrementalCompute()
        }
    }

    open protected fun initialCompute(): Double = internalCompute()

    protected fun incrementalCompute(): Double = internalCompute()

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
