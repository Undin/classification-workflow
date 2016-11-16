package com.warrior.classification_workflow.meta_learning.metafeatures.informationtheoretic

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractAttributeMetaFeature
import com.warrior.classification_workflow.meta_learning.metafeatures.Mean
import weka.core.Attribute
import weka.core.Instances

/**
 * Created by warrior on 23.03.15.
 */
class NoiseSignalRatio : AbstractAttributeMetaFeature(Mean) {

    val meanMutualInformation: MeanMutualInformation = MeanMutualInformation()

    override var instances: Instances
        get() = super.instances
        set(value) {
            super.instances = value
            meanMutualInformation.instances = value
        }

    override fun computeAttributeValue(attribute: Attribute): Double = entropy(instances, attribute).entropy

    override fun compute(): Double {
        val meanEntropy = super.compute()
        val mutualInfo = meanMutualInformation.compute()
        return (meanEntropy - mutualInfo) / mutualInfo
    }
}
