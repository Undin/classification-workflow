package com.warrior.classification_workflow.core.meta.features.informationtheoretic

import com.warrior.classification_workflow.core.meta.features.*
import weka.attributeSelection.InfoGainAttributeEval
import weka.core.Attribute
import java.util.*
import java.util.concurrent.ConcurrentMap

/**
 * Created by warrior on 11/18/16.
 */
abstract class MutualInformation(aggregator: Aggregator) :
        AbstractAttributeMetaFeatureExtractor(aggregator), MutualInformationCache {

    protected var cache: MutableMap<Attribute, Double>? = null

    override fun setMutualInformationCache(cache: ConcurrentMap<Attribute, Double>) {
        this.cache = cache
    }

    override fun compute(): Double {
        return if (attributeMap.isEmpty() && cache?.isEmpty() ?: true) {
            initialCompute()
        } else {
            incrementalCompute()
        }
    }

    override fun computeAttributeValue(attribute: Attribute): Double {
        return getFromCacheOrPut(attribute) { mutualInformation(instances, attribute) }
    }

    override fun initialCompute(): Double {
        val infoGain = InfoGainAttributeEval()
        infoGain.buildEvaluator(instances)

        val values = ArrayList<Double>(instances.numAttributes() - 1)
        for (attr in instances.enumerateAttributes()) {
            if (isSuitable(attr)) {
                val value = infoGain.evaluateAttribute(attr.index())
                attributeMap[attr] = value
                cache?.put(attr, value)
                values += value
            }
        }
        return aggregator.aggregate(values)
    }

    protected inline fun getFromCacheOrPut(attribute: Attribute, defaultValue: () -> Double): Double {
        return cache?.getOrPut(attribute, defaultValue) ?: defaultValue()
    }
}

class MaxMutualInformation : MutualInformation(Max)
class MeanMutualInformation : MutualInformation(Mean)
