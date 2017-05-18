package com.warrior.classification_workflow.baseline.single.randomSearch.ext

import weka.classifiers.Evaluation
import weka.classifiers.meta.multisearch.DefaultEvaluationWrapper

class DefaultEvaluationWrapperExt(eval: Evaluation, metrics: DefaultEvaluationMetricsExt) : DefaultEvaluationWrapper(eval, metrics) {

    override fun getMetric(id: Int, classLabel: Int): Double {
        return if (id == DefaultEvaluationMetricsExt.EVALUATION_UNWEIGHTED_MACRO_FMEASURE) {
            m_Evaluation.unweightedMacroFmeasure()
        } else super.getMetric(id, classLabel)
    }
}
