package com.warrior.classification_workflow.core.meta.features.general

import com.warrior.classification_workflow.core.meta.features.AbstractMetaFeature

/**
 * Created by warrior on 22.03.15.
 */
class NumberOfFeatures : AbstractMetaFeature() {

    override fun compute(): Double {
        val numAttributes = instances.numAttributes().toDouble()
        return if (instances.classIndex() >= 0) numAttributes - 1 else numAttributes
    }
}
