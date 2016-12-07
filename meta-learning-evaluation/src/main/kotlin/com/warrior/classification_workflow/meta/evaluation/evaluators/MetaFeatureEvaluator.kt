package com.warrior.classification_workflow.meta.evaluation.evaluators

import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.core.meta.features.CommonMetaFeatureExtractor
import com.warrior.classification_workflow.meta.evaluation.*
import weka.core.Instances
import java.io.File
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

/**
 * Created by warrior on 11/28/16.
 */
class MetaFeatureEvaluator(private val config: MetaFeatureConfig, pool: ForkJoinPool)
    : AbstractPerformanceEvaluator(pool) {

    override val saveStrategy: SaveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)

    override fun getTasks(): List<ForkJoinTask<*>> {
        val datasets = config.datasets.map { File(config.datasetFolder, it) }
        return datasets.map { dataset ->
            ForkJoinTask.adapt {
                val data = load(dataset.absolutePath)
                calculate(data, saveStrategy)
            }
        }
    }

    private fun calculate(data: Instances, saveStrategy: SaveStrategy) {
        val extractor = logger.withLog("extract meta-features: ${data.relationName()}") {
            CommonMetaFeatureExtractor(data)
        }

        val result = extractor.extract()
        val entity = MetaFeaturesEntity(result)
        saveStrategy.save(entity)
    }
}
