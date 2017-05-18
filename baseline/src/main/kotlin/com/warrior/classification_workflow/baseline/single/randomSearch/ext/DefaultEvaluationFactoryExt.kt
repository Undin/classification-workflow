package com.warrior.classification_workflow.baseline.single.randomSearch.ext

import com.warrior.classification_workflow.baseline.single.randomSearch.Result
import weka.classifiers.Evaluation
import weka.classifiers.meta.MultiSearch
import weka.classifiers.meta.multisearch.DefaultEvaluationFactory
import weka.core.Instances
import weka.core.SetupGenerator
import weka.core.setupgenerator.Point

class DefaultEvaluationFactoryExt(
        private val startCPUTime: Long,
        private val maxCPUTime: Long,
        private val output: MutableList<Result>
) : DefaultEvaluationFactory() {

    override fun newWrapper(eval: Evaluation): DefaultEvaluationWrapperExt
            = DefaultEvaluationWrapperExt(eval, newMetrics())

    override fun newMetrics(): DefaultEvaluationMetricsExt
            = DefaultEvaluationMetricsExt()

    override fun newTask(owner: MultiSearch, train: Instances, test: Instances?, generator: SetupGenerator, values: Point<Any>, folds: Int, eval: Int, classLabel: Int): DefaultEvaluationTaskExt {
        return DefaultEvaluationTaskExt(startCPUTime, maxCPUTime, output, owner, train, test, generator, values, folds, eval, classLabel)
    }
}
