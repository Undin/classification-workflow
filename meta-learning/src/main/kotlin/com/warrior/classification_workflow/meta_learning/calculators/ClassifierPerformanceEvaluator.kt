package com.warrior.classification_workflow.meta_learning.calculators

import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.meta_learning.*
import weka.core.Instances
import java.io.File
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

/**
 * Created by warrior on 11/28/16.
 */
class ClassifierPerformanceEvaluator(private val config: ClassifierPerfConfig, pool: ForkJoinPool)
    : AbstractPerformanceEvaluator(pool) {

    override val saveStrategy: SaveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)

    override fun getTasks(): List<ForkJoinTask<*>> {
        val datasets = config.datasets.map { File(config.datasetFolder, it) }
        val random = Random()
        return datasets.map { dataset ->
            ForkJoinTask.adapt {
                logger.withLog("start $dataset") {
                    val data = load(dataset.absolutePath)
                    config.classifiers.forEachParallel { calculate(it, data, random, saveStrategy) }
                }
            }
        }
    }

    private fun calculate(classifier: Classifier, data: Instances, random: Random, saveStrategy: SaveStrategy) {
        val measure = logger.withLog("evaluate ${classifier.name} on ${data.relationName()}") {
            crossValidation(classifier, data, random)
        }

        val entity = ClassifierPerformanceEntity(classifier.name, data.relationName(), measure)
        saveStrategy.save(entity)
    }
}
