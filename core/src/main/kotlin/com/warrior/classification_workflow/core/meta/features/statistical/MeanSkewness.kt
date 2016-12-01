package com.warrior.classification_workflow.core.meta.features.statistical

import com.warrior.classification_workflow.core.meta.features.AbstractAttributeMetaFeatureExtractor
import com.warrior.classification_workflow.core.meta.features.Mean
import org.apache.commons.math3.stat.descriptive.moment.Skewness
import weka.core.Attribute

/**
 * Created by warrior on 22.03.15.
 */
class MeanSkewness : AbstractAttributeMetaFeatureExtractor(Mean) {

    override fun computeAttributeValue(attribute: Attribute): Double {
        val values = instances.attributeToDoubleArray(attribute.index())
        val skewness = org.apache.commons.math3.stat.descriptive.moment.Skewness()
        return skewness.evaluate(values)
    }
}
