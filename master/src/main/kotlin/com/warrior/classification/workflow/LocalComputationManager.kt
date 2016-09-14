package com.warrior.classification.workflow

import com.warrior.classification.workflow.core.ComputationManager
import com.warrior.classification.workflow.core.Workflow
import com.warrior.classification.workflow.core.load
import kotlinx.support.jdk8.collections.parallelStream
import weka.classifiers.evaluation.Evaluation
import weka.core.Instances
import java.util.*
import java.util.stream.Collectors

/**
 * Created by warrior on 29/06/16.
 */
class LocalComputationManager : ComputationManager {

    private val datasetFolder = "datasets"
    private val numFolds = 10
    private val random = Random()

    override fun compute(tasks: List<Workflow>, dataset: String): List<ComputationManager.Result> {
        val instances = load("$datasetFolder/$dataset")
        return tasks.parallelStream()
                .map { w -> ComputationManager.Result(w, compute(w, instances)) }
                .collect(Collectors.toList<ComputationManager.Result>())
    }

    private fun compute(workflow: Workflow, instances: Instances): Double {
        val eval = Evaluation(instances)
        try {
            eval.crossValidateModel(workflow, instances, numFolds, random)
        } catch (e: Exception) {
            e.printStackTrace()
            return 0.0
        }
        return eval.unweightedMacroFmeasure()
    }
}
