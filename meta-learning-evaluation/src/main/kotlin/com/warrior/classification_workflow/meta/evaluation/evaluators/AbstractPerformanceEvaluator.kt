package com.warrior.classification_workflow.meta.evaluation.evaluators

import com.warrior.classification_workflow.core.Classifier
import com.warrior.classification_workflow.core.parallelCrossValidation
import com.warrior.classification_workflow.core.storage.SaveStrategy
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import weka.core.Instances
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

/**
 * Created by warrior on 11/28/16.
 */
abstract class AbstractPerformanceEvaluator(private val pool: ForkJoinPool) : Evaluator {

    private val crossValidationFolders = 10

    protected val logger: Logger = LogManager.getLogger(javaClass)

    protected abstract val saveStrategy: SaveStrategy

    override fun evaluate() {
        val tasks = getTasks()
        try {
            for (task in tasks) {
                pool.submit(task)
            }
            for (task in tasks) {
                task.get()
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
        } finally {
            saveStrategy.close()
        }
    }

    protected fun crossValidation(classifier: Classifier, data: Instances, random: Random): Double {
        val evaluation = parallelCrossValidation(classifier, data, crossValidationFolders, random, logger)
        return evaluation.unweightedMacroFmeasure()
    }

    protected abstract fun getTasks(): List<ForkJoinTask<*>>
}
