package com.warrior.classification_workflow.core.meta.features.informationtheoretic

import com.warrior.classification_workflow.core.meta.features.AbstractAttributeMetaFeature
import com.warrior.classification_workflow.core.meta.features.Aggregator
import com.warrior.classification_workflow.core.meta.features.Mean
import weka.core.Attribute
import weka.filters.Filter
import weka.filters.supervised.attribute.Discretize
import java.util.*

/**
 * Created by warrior on 11/18/16.
 */
abstract class EntropyMetaFeature(aggregator: Aggregator) :
        AbstractAttributeMetaFeature(aggregator), EntropyCache {

    protected var cache: MutableMap<Attribute, EntropyResult>? = null

    override fun setEntropyCache(cache: MutableMap<Attribute, EntropyResult>) {
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
        val entropyResult = getFromCacheOrPut(attribute) { entropy(instances, attribute) }
        return entropyResult.value()
    }

    override fun initialCompute(): Double {
        val discretize = Discretize()
        discretize.useBetterEncoding = true
        discretize.setInputFormat(instances)
        val discreteInstances = Filter.useFilter(instances, discretize)

        val values = ArrayList<Double>(instances.numAttributes() - 1)
        for (attr in instances.enumerateAttributes()) {
            if (isSuitable(attr)) {
                val discreteAttribute = discreteInstances.attribute(attr.index())
                val attributeValues = discreteInstances.attributeToDoubleArray(attr.index())
                val entropyResult = entropy(attributeValues, discreteAttribute.numValues())
                cache?.put(attr, entropyResult)
                val value = entropyResult.value()
                attributeMap[attr] = value
                values += value
            }
        }

        return aggregator.aggregate(values)
    }

    protected inline fun getFromCacheOrPut(attribute: Attribute, defaultValue: () -> EntropyResult): EntropyResult {
        return cache?.getOrPut(attribute, defaultValue) ?: defaultValue()
    }

    abstract protected fun EntropyResult.value(): Double
}

class MeanNormalizedFeatureEntropy : EntropyMetaFeature(Mean) {
    override fun EntropyResult.value(): Double = normalizedEntropy
}

class MeanFeatureEntropy : EntropyMetaFeature(Mean) {
    override fun EntropyResult.value(): Double = entropy
}
