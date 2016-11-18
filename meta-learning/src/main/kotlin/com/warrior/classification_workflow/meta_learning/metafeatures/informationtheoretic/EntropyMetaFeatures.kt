package com.warrior.classification_workflow.meta_learning.metafeatures.informationtheoretic

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractAttributeMetaFeature
import com.warrior.classification_workflow.meta_learning.metafeatures.Aggregator
import com.warrior.classification_workflow.meta_learning.metafeatures.Mean
import weka.core.Attribute
import weka.filters.Filter
import weka.filters.supervised.attribute.Discretize
import java.util.*

/**
 * Created by warrior on 11/18/16.
 */
abstract class EntropyMetaFeature(aggregator: Aggregator) : AbstractAttributeMetaFeature(aggregator) {

    override fun computeAttributeValue(attribute: Attribute): Double = entropy(instances, attribute).normalizedEntropy

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
                val value = entropy(attributeValues, discreteAttribute.numValues()).value()
                attributeMap[attr] = value
                values += value
            }
        }

        return aggregator.aggregate(values)
    }

    abstract protected fun EntropyResult.value(): Double
}

class MeanNormalizedFeatureEntropy : EntropyMetaFeature(Mean) {
    override fun EntropyResult.value(): Double = normalizedEntropy
}

class MeanFeatureEntropy : EntropyMetaFeature(Mean) {
    override fun EntropyResult.value(): Double = entropy
}
