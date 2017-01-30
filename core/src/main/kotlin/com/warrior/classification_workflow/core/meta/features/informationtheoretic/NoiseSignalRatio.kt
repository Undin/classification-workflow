package com.warrior.classification_workflow.core.meta.features.informationtheoretic

import com.warrior.classification_workflow.core.meta.features.AbstractMetaFeatureExtractor
import weka.core.Attribute
import weka.core.Instances
import java.util.concurrent.ConcurrentMap

/**
 * Created by warrior on 23.03.15.
 */
class NoiseSignalRatio : AbstractMetaFeatureExtractor(), MutualInformationCache, EntropyCache {

    val meanMutualInformation: MeanMutualInformation = MeanMutualInformation()
    val meanEntropy: MeanFeatureEntropy = MeanFeatureEntropy()

    override var instances: Instances
        get() = super.instances
        set(value) {
            super.instances = value
            meanMutualInformation.instances = value
            meanEntropy.instances = value
        }

    override fun setMutualInformationCache(cache: ConcurrentMap<Attribute, Double>) {
        meanMutualInformation.setMutualInformationCache(cache)
    }

    override fun setEntropyCache(cache: ConcurrentMap<Attribute, EntropyResult>) {
        meanEntropy.setEntropyCache(cache)
    }

    override fun compute(): Double {
        val mutualInfo = meanMutualInformation.compute()
        val meanEntropy = meanEntropy.compute()
        return when {
            meanEntropy == mutualInfo -> 0.0
            mutualInfo == 0.0 -> Double.MAX_VALUE
            else -> (meanEntropy - mutualInfo) / mutualInfo
        }
    }
}
