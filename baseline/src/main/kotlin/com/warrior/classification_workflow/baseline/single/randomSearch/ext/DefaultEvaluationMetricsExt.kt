package com.warrior.classification_workflow.baseline.single.randomSearch.ext

import weka.classifiers.meta.multisearch.DefaultEvaluationMetrics
import weka.core.Tag

class DefaultEvaluationMetricsExt : DefaultEvaluationMetrics() {

    override fun getTags(): Array<Tag> = TAGS
    override fun getDefaultMetric(): Int = EVALUATION_UNWEIGHTED_MACRO_FMEASURE
    override fun invert(id: Int): Boolean {
        return if (id == EVALUATION_UNWEIGHTED_MACRO_FMEASURE) true else super.invert(id)
    }

    companion object {
        const val EVALUATION_UNWEIGHTED_MACRO_FMEASURE: Int = 23

        val TAGS: Array<Tag> = TAGS_EVALUATION + Tag(EVALUATION_UNWEIGHTED_MACRO_FMEASURE, "UMFM", "Unweighted Macro F-Measure")
    }
}
