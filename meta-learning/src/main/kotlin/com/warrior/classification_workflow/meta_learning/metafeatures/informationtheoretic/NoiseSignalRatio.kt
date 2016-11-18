package com.warrior.classification_workflow.meta_learning.metafeatures.informationtheoretic

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractMetaFeature
import weka.core.Instances

/**
 * Created by warrior on 23.03.15.
 */
class NoiseSignalRatio : AbstractMetaFeature() {

    val meanMutualInformation: MeanMutualInformation = MeanMutualInformation()
    val meanEntropy: MeanFeatureEntropy = MeanFeatureEntropy()

    override var instances: Instances
        get() = super.instances
        set(value) {
            super.instances = value
            meanMutualInformation.instances = value
            meanEntropy.instances = value
        }

    override fun compute(): Double {
        val mutualInfo = meanMutualInformation.compute()
        val meanEntropy = meanEntropy.compute()
        return (meanEntropy - mutualInfo) / mutualInfo
    }
}
