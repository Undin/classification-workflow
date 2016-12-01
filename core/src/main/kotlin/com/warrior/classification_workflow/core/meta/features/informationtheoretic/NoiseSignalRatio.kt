package com.warrior.classification_workflow.core.meta.features.informationtheoretic

import com.warrior.classification_workflow.core.meta.features.AbstractMetaFeature
import weka.core.Attribute
import weka.core.Instances

/**
 * Created by warrior on 23.03.15.
 */
class NoiseSignalRatio : AbstractMetaFeature(), MutualInformationCache, EntropyCache {

    val meanMutualInformation: MeanMutualInformation = MeanMutualInformation()
    val meanEntropy: MeanFeatureEntropy = MeanFeatureEntropy()

    override var instances: Instances
        get() = super.instances
        set(value) {
            super.instances = value
            meanMutualInformation.instances = value
            meanEntropy.instances = value
        }

    override fun setMutualInformationCache(cache: MutableMap<Attribute, Double>) {
        meanMutualInformation.setMutualInformationCache(cache)
    }

    override fun setEntropyCache(cache: MutableMap<Attribute, EntropyResult>) {
        meanEntropy.setEntropyCache(cache)
    }

    override fun compute(): Double {
        val mutualInfo = meanMutualInformation.compute()
        val meanEntropy = meanEntropy.compute()
        return (meanEntropy - mutualInfo) / mutualInfo
    }
}
