package com.warrior.classification_workflow.meta_learning.calculators

import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.meta_learning.*
import com.warrior.classification_workflow.meta_learning.metafeatures.MetaFeatureExtractor
import org.apache.logging.log4j.LogManager
import weka.core.Instances
import java.io.File

/**
 * Created by warrior on 11/28/16.
 */
class MetaFeatureEvaluator(private val config: MetaFeatureConfig) : AbstractPerformanceEvaluator() {

    private val logger = LogManager.getLogger(MetaFeatureEvaluator::class.java)

    override fun evaluate() {
        val datasets = config.datasets.map { File(config.datasetFolder, it) }
        val saveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)

        saveStrategy.use { saveStrategy ->
            datasets.forEachParallel { file ->
                val data = load(file.absolutePath)
                calculate(data, saveStrategy)
            }
        }
    }

    private fun calculate(data: Instances, saveStrategy: SaveStrategy) {
        val extractor = logger.withLog("extract meta-features: ${data.relationName()}") {
            MetaFeatureExtractor(data)
        }

        val result = extractor.extract()
        val entity = MetaFeaturesEntity(data.relationName(), result)
        saveStrategy.save(entity)
    }
}
