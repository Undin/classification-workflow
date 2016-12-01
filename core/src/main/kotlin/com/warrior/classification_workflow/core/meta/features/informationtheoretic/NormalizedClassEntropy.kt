package com.warrior.classification_workflow.core.meta.features.informationtheoretic

import com.warrior.classification_workflow.core.meta.features.AbstractMetaFeature
import weka.core.Instances

/**
 * Created by warrior on 23.03.15.
 */
class NormalizedClassEntropy : AbstractMetaFeature() {

    override var instances: Instances
        get() = super.instances
        set(value) {
            require(value.classIndex() >= 0) { "dataset doesn't have class attribute" }
            super.instances = value
        }

    private var normalizedEntropy: Lazy<Double> = lazy {
        val classIndex = instances.classIndex()
        val values = instances.attributeToDoubleArray(classIndex)
        val result = entropy(values, instances.classAttribute().numValues())
        result.normalizedEntropy
    }

    override fun compute(): Double = normalizedEntropy.value
}
