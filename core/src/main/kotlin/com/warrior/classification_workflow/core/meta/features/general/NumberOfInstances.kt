package com.warrior.classification_workflow.core.meta.features.general

import com.warrior.classification_workflow.core.meta.features.AbstractMetaFeature

/**
 * Created by warrior on 22.03.15.
 */
class NumberOfInstances : AbstractMetaFeature() {
    override fun compute(): Double = instances.numInstances().toDouble()
}
