package com.warrior.classification_workflow.meta_learning.metafeatures.general

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractMetaFeature

/**
 * Created by warrior on 22.03.15.
 */
class DataSetDimensionality : AbstractMetaFeature() {
    override fun compute(): Double {
        val instanceNumber = instances.numInstances()
        val attributeNumber = if (instances.classIndex() >= 0) instances.numAttributes() - 1 else instances.numAttributes()
        return instanceNumber.toDouble() / attributeNumber
    }
}
