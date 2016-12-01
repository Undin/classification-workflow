package com.warrior.classification_workflow.core.meta.features.statistical

import com.warrior.classification_workflow.core.meta.features.AbstractAttributeMetaFeature
import com.warrior.classification_workflow.core.meta.features.Mean
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis
import weka.core.Attribute

/**
 * Created by warrior on 22.03.15.
 */
class MeanKurtosis : AbstractAttributeMetaFeature(Mean) {

    override fun computeAttributeValue(attribute: Attribute): Double {
        val values = instances.attributeToDoubleArray(attribute.index())
        val kurtosis = org.apache.commons.math3.stat.descriptive.moment.Kurtosis()
        return kurtosis.evaluate(values)
    }
}
