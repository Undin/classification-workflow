package com.warrior.classification_workflow.meta_learning.calculators

import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.meta_learning.*
import org.apache.logging.log4j.LogManager
import weka.core.Instances
import java.io.File
import java.util.*

/**
 * Created by warrior on 11/28/16.
 */
class ClassifierPerformanceEvaluator(private val config: ClassifierPerfConfig) : AbstractPerformanceEvaluator() {

    private val logger = LogManager.getLogger(ClassifierPerformanceEvaluator::class.java)

    override fun evaluate() {
        val datasets = config.datasets.map { File(config.datasetFolder, it) }
        val saveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)

        val random = Random()
        saveStrategy.use { saveStrategy ->
            datasets.forEachParallel { dataset ->
                val data = load(dataset.absolutePath)
                config.classifiers.forEachParallel { calculate(it, data, random, saveStrategy) }
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
