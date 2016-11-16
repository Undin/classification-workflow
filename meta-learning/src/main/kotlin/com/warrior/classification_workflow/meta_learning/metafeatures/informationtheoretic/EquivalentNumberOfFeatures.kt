package com.warrior.classification_workflow.meta_learning.metafeatures.informationtheoretic

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractMetaFeature
import weka.core.Instances

/**
 * Created by warrior on 23.03.15.
 */
class EquivalentNumberOfFeatures : AbstractMetaFeature() {

    val meanMutualInformation: MeanMutualInformation = MeanMutualInformation()

    override var instances: Instances
        get() = super.instances
        set(value) {
            super.instances = value
            meanMutualInformation.instances = value
        }

    private var classEntropy: Lazy<Double> = lazy {
        val classIndex = instances.classIndex()
        val values = instances.attributeToDoubleArray(classIndex)
        val result = entropy(values, instances.classAttribute().numValues())
        result.entropy
    }

    override fun compute(): Double = classEntropy.value / meanMutualInformation.compute()
}
