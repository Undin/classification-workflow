package com.warrior.classification_workflow.meta_learning.metafeatures.statistical

import com.warrior.classification_workflow.meta_learning.metafeatures.AbstractAttributeMetaFeature
import com.warrior.classification_workflow.meta_learning.metafeatures.Mean
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import weka.core.Attribute

/**
 * Created by warrior on 16.05.15.
 */
class MeanCoefficientOfVariation : AbstractAttributeMetaFeature(Mean) {

    override fun computeAttributeValue(attribute: Attribute): Double {
        val values = instances.attributeToDoubleArray(attribute.index())
        val mean = values.average()
        val std = StandardDeviation()
        return std.evaluate(values, mean) / mean
    }
}
