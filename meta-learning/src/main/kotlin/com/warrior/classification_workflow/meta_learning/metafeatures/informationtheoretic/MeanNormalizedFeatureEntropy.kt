package com.warrior.classification_workflow.meta_learning.metafeatures.informationtheoretic

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractAttributeMetaFeature
import com.warrior.classification_workflow.meta_learning.metafeatures.Mean
import weka.core.Attribute

/**
 * Created by warrior on 23.03.15.
 */
class MeanNormalizedFeatureEntropy : AbstractAttributeMetaFeature(Mean) {
    override fun computeAttributeValue(attribute: Attribute): Double = entropy(instances, attribute).normalizedEntropy
}
