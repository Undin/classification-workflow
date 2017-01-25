package com.warrior.classification_workflow.meta.evaluation.evaluators

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.warrior.classification_workflow.core.Classifier
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.meta.evaluation.*
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
        val currentResults = currentResults()
        val random = Random()
        val saveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)

        val tasks = datasets.map { dataset ->
            ForkJoinTask.adapt {
                val datasetSet = currentResults[dataset.nameWithoutExtension] ?: emptySet()
                val classifiers = config.classifiers.filter { it.name !in datasetSet }
                if (classifiers.isNotEmpty()) {
                    logger.withLog("start $dataset") {
                        val data = load(dataset.absolutePath)
                        classifiers.forEachParallel { calculate(it, data, random, saveStrategy) }
                    }
                }
            }
        }
        return tasks
    }

    private fun currentResults(): Map<String, Set<String>> {
        val results = if (config.currentResults != null) {
            val mapper = jacksonObjectMapper()
            try {
                mapper.readValue<List<ClassifierPerformanceEntity>>(File(config.currentResults))
            } catch (e: Exception) {
                logger.error(e.message, e)
                emptyList<ClassifierPerformanceEntity>()
            }
        } else {
            emptyList()
        }

        val resultMap = HashMap<String, MutableSet<String>>(results.size)
        for (result in results) {
            val datasetMap = resultMap.getOrPut(result.datasetName) { HashSet() }
            datasetMap += result.classifierName
        }
        return resultMap
    }

    private fun calculate(classifier: Classifier, data: Instances, random: Random, saveStrategy: SaveStrategy) {
        val measure = logger.withLog("evaluate ${classifier.name} on ${data.relationName()}") {
            crossValidation(classifier, data, random)
        }

        val entity = ClassifierPerformanceEntity(classifier.name, data.relationName(), measure)
        saveStrategy.save(entity)
    }
}
