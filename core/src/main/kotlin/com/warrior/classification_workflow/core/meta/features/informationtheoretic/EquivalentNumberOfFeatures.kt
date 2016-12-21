package com.warrior.classification_workflow.core.meta.features.informationtheoretic

import com.warrior.classification_workflow.core.meta.features.AbstractMetaFeatureExtractor
import weka.core.Attribute
import weka.core.Instances
import java.util.concurrent.ConcurrentMap

/**
 * Created by warrior on 23.03.15.
 */
class EquivalentNumberOfFeatures : AbstractMetaFeatureExtractor(), MutualInformationCache {

    val meanMutualInformation: MeanMutualInformation = MeanMutualInformation()

    override var instances: Instances
        get() = super.instances
        set(value) {
            require(value.classIndex() >= 0) { "dataset doesn't have class attribute" }
            super.instances = value
            meanMutualInformation.instances = value
        }

    private var classEntropy: Lazy<Double> = lazy {
        val classIndex = instances.classIndex()
        val values = instances.attributeToDoubleArray(classIndex)
        val result = entropy(values, instances.classAttribute().numValues())
        result.entropy
    }

    override fun setMutualInformationCache(cache: ConcurrentMap<Attribute, Double>) {
        meanMutualInformation.setMutualInformationCache(cache)
    }

    override fun compute(): Double = classEntropy.value / meanMutualInformation.compute()
}
