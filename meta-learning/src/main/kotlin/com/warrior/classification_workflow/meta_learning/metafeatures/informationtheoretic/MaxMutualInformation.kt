package com.warrior.classification_workflow.meta_learning.metafeatures.informationtheoretic

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractAttributeMetaFeature
import com.warrior.classification_workflow.meta_learning.metafeatures.Max
import weka.core.Attribute

/**
 * Created by warrior on 23.03.15.
 */
class MaxMutualInformation : AbstractAttributeMetaFeature(Max) {

    override fun computeAttributeValue(attribute: Attribute): Double = mutualInformation(instances, attribute)
}
