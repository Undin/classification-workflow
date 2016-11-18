package com.warrior.classification_workflow.meta_learning.metafeatures.informationtheoretic

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractAttributeMetaFeature
import com.warrior.classification_workflow.meta_learning.metafeatures.Aggregator
import com.warrior.classification_workflow.meta_learning.metafeatures.Max
import com.warrior.classification_workflow.meta_learning.metafeatures.Mean
import weka.attributeSelection.InfoGainAttributeEval
import weka.core.Attribute
import java.util.*

/**
 * Created by warrior on 11/18/16.
 */
abstract class MutualInformation(aggregator: Aggregator) : AbstractAttributeMetaFeature(aggregator) {

    override fun computeAttributeValue(attribute: Attribute): Double = mutualInformation(instances, attribute)

    override fun initialCompute(): Double {
        val infoGain = InfoGainAttributeEval()
        infoGain.buildEvaluator(instances)

        val values = ArrayList<Double>(instances.numAttributes() - 1)
        for (attr in instances.enumerateAttributes()) {
            if (isSuitable(attr)) {
                val value = infoGain.evaluateAttribute(attr.index())
                attributeMap[attr] = value
                values += value
            }
        }
        return aggregator.aggregate(values)
    }
}

class MaxMutualInformation : MutualInformation(Max)
class MeanMutualInformation : MutualInformation(Mean)
