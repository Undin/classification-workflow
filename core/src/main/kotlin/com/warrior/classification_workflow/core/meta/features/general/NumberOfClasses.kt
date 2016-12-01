package com.warrior.classification_workflow.core.meta.features.general

import com.warrior.classification_workflow.core.meta.features.AbstractMetaFeature

/**
 * Created by warrior on 22.03.15.
 */
class NumberOfClasses : AbstractMetaFeature() {

    override fun compute(): Double {
        return if (instances.classIndex() >= 0) instances.numClasses().toDouble() else 0.0
    }
}
