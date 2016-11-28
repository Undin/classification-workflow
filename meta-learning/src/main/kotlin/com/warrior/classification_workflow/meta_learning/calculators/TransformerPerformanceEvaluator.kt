package com.warrior.classification_workflow.meta_learning.calculators

import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.meta_learning.*
import org.apache.logging.log4j.LogManager
import weka.attributeSelection.ASEvaluation
import weka.attributeSelection.ASSearch
import weka.core.Instances
import weka.filters.Filter
import weka.filters.supervised.attribute.AttributeSelection
import java.io.File
import java.util.*

/**
 * Created by warrior on 11/28/16.
 */
class TransformerPerformanceEvaluator(private val config: TransformerPerfConfig) : AbstractPerformanceEvaluator() {

    private val logger = LogManager.getLogger(TransformerPerformanceEvaluator::class.java)

    override fun evaluate() {
        val datasets = config.datasets.map { File(config.datasetFolder, it) }
        val saveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)
        val random = Random()

        saveStrategy.use { saveStrategy ->
            datasets.forEachParallel { dataset ->
                val data = load(dataset.absolutePath)
                config.transformers.forEachParallel { transformer ->
                    calculate(transformer, config.classifiers, data, random, saveStrategy)
                }
            }
        }
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
