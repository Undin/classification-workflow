package com.warrior.classification_workflow.meta_learning.metafeatures.general

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractMetaFeature

/**
 * Created by warrior on 22.03.15.
 */
class NumberOfInstances : AbstractMetaFeature() {
    override fun compute(): Double = instances.numInstances().toDouble()
}
