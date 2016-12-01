package com.warrior.classification_workflow.core.meta.features.general

import com.warrior.classification_workflow.core.meta.features.AbstractMetaFeatureExtractor

/**
 * Created by warrior on 22.03.15.
 */
class DataSetDimensionality : AbstractMetaFeatureExtractor() {
    override fun compute(): Double {
        val instanceNumber = instances.numInstances()
        val attributeNumber = if (instances.classIndex() >= 0) instances.numAttributes() - 1 else instances.numAttributes()
        return instanceNumber.toDouble() / attributeNumber
    }
}
