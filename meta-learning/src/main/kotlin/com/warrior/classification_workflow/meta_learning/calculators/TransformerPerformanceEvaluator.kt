package com.warrior.classification_workflow.meta_learning.calculators

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.meta_learning.*
import weka.attributeSelection.ASEvaluation
import weka.attributeSelection.ASSearch
import weka.core.Instances
import weka.filters.Filter
import weka.filters.supervised.attribute.AttributeSelection
import java.io.File
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

/**
 * Created by warrior on 11/28/16.
 */
class TransformerPerformanceEvaluator(private val config: TransformerPerfConfig, pool: ForkJoinPool)
    : AbstractPerformanceEvaluator(pool) {

    override val saveStrategy: SaveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)

    override fun getTasks(): List<ForkJoinTask<*>> {
        val datasets = config.datasets.map { File(config.datasetFolder, it) }
        val currentResults = currentResults()
        val random = Random()

        val task = ForkJoinTask.adapt {
            for (dataset in datasets) {
                val datasetMap = currentResults.getOrElse(dataset.nameWithoutExtension) { emptyMap() }

                val needCalculate = config.transformers.any { transformer ->
                    val transformerSet = datasetMap[transformer.name]
                    transformerSet == null || config.classifiers.any { it.name !in transformerSet}
                }

                if (needCalculate) {
                    logger.withLog("start $dataset") {
                        val data = load(dataset.absolutePath)
                        for (transformer in config.transformers) {
                            val transformerSet = datasetMap[transformer.name] ?: emptySet()
                            val classifiers = config.classifiers.filter { it.name !in transformerSet }
                            if (classifiers.isNotEmpty()) {
                                calculate(transformer, classifiers, data, random, saveStrategy)
                            }
                        }
                    }
                }
            }
        }
        return listOf(task)
    }

    private fun currentResults(): Map<String, Map<String, Set<String>>> {
        val results = if (config.currentResults != null) {
            val mapper = ObjectMapper().registerKotlinModule()
            try {
                mapper.readValue<List<TransformerPerformanceEntity>>(File(config.currentResults))
            } catch (e: Exception) {
                logger.error(e.message, e)
                emptyList<TransformerPerformanceEntity>()
            }
        } else {
            emptyList()
        }

        val resultMap = HashMap<String, MutableMap<String, MutableSet<String>>>(results.size)
        for (result in results) {
            val datasetMap = resultMap.getOrPut(result.datasetName) { HashMap() }
            val transformerSet = datasetMap.getOrPut(result.transformerName) { HashSet() }
            transformerSet += result.classifierName
        }
        return resultMap
    }

    private fun calculate(transformer: Transformer, classifiers: List<Classifier>, data: Instances,
                          random: Random, saveStrategy: SaveStrategy) {
        val transformedData = logger.withLog("${transformer.name} on ${data.relationName()}") {
            transform(transformer, data)
        }

        classifiers.forEachParallel { classifier ->
            val measure = logger.withLog("evaluate ${classifier.name} on ${data.relationName()}") {
                crossValidation(classifier, transformedData, random)
            }

            val entity = TransformerPerformanceEntity(transformer.name, classifier.name,
                    data.relationName(), measure)
            saveStrategy.save(entity)
        }
    }

    fun transform(transformer: Transformer, data: Instances): Instances {
        val (name, search, evaluation) = transformer
        val filter = AttributeSelection()
        filter.search = ASSearch.forName(search.className, search.options.toArray())
        filter.evaluator = ASEvaluation.forName(evaluation.className, evaluation.options.toArray())
        filter.setInputFormat(data)
        return Filter.useFilter(data, filter).apply { setRelationName(data.relationName()) }
    }
}
