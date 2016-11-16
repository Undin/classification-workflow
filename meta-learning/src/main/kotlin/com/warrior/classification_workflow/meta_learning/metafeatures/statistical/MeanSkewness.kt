package com.warrior.classification_workflow.meta_learning.metafeatures.statistical

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractAttributeMetaFeature
import com.warrior.classification_workflow.meta_learning.metafeatures.Mean
import org.apache.commons.math3.stat.descriptive.moment.Skewness
import weka.core.Attribute

/**
 * Created by warrior on 22.03.15.
 */
class MeanSkewness : AbstractAttributeMetaFeature(Mean) {

    override fun computeAttributeValue(attribute: Attribute): Double {
        val values = instances.attributeToDoubleArray(attribute.index())
        val skewness = Skewness()
        return skewness.evaluate(values)
    }
}
