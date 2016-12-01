package com.warrior.classification_workflow.core.meta.features.general

import com.warrior.classification_workflow.core.meta.features.AbstractMetaFeatureExtractor

/**
 * Created by warrior on 22.03.15.
 */
class NumberOfInstances : AbstractMetaFeatureExtractor() {
    override fun compute(): Double = instances.numInstances().toDouble()
}
