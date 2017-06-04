package com.warrior.classification_workflow.experiments.evaluation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.warrior.classification_workflow.Config
import com.warrior.classification_workflow.WorkflowConstructor
import com.warrior.classification_workflow.core.meta.entity.ClassifierPerformanceEntity
import com.warrior.classification_workflow.core.meta.entity.MetaFeaturesEntity
import com.warrior.classification_workflow.core.meta.entity.TransformerPerformanceEntity
import com.warrior.classification_workflow.core.meta.features.CommonMetaFeatureExtractor
import com.warrior.classification_workflow.meta.AlgorithmChooser
import com.warrior.classification_workflow.meta.Selector
import weka.core.Instances
import java.io.File
import java.util.HashSet

class NoInnerClassifierWorkflowConstructor(config: Config) : WorkflowConstructor(config) {
    override fun algorithmChooser(config: Config, datasetName: String, selector: Selector): AlgorithmChooser {
        val jsonMapper = jacksonObjectMapper()
        val paths = config.metaDataPaths
        val metaFeatures: List<MetaFeaturesEntity> = jsonMapper.readValue(File(paths.metaFeaturesPath))
        val classifierPerformance: List<ClassifierPerformanceEntity> = jsonMapper.readValue(File(paths.classifierPerformancePath))
        val transformerPerformance: List<TransformerPerformanceEntity> = jsonMapper.readValue(File(paths.transformerPerformancePath))

        val datasets = metaFeatures.mapTo(HashSet()) { it.datasetName }
        datasets.remove(datasetName)

        val classifiers = classifierPerformance.mapTo(HashSet()) { it.classifierName }
        val transformers = transformerPerformance.mapTo(HashSet()) { it.transformerName }

        val algorithmChooser = NoInnerClassifierAlgorithmChooser(
                datasets = datasets,
                classifiers = classifiers,
                transformers = transformers,
                metaFeatures = metaFeatures,
                classifierPerformance = classifierPerformance,
                transformerPerformance = transformerPerformance,
                selector = selector
        )
        return algorithmChooser
    }
}

private class NoInnerClassifierAlgorithmChooser(
        datasets: Set<String>,
        classifiers: Set<String>,
        transformers: Set<String>,
        metaFeatures: List<MetaFeaturesEntity>,
        classifierPerformance: List<ClassifierPerformanceEntity>,
        transformerPerformance: List<TransformerPerformanceEntity>,
        selector: Selector
) : AlgorithmChooser(datasets,
        classifiers,
        transformers,
        metaFeatures,
        classifierPerformance,
        transformerPerformance,
        selector) {

    override fun chooseAlgorithm(extractor: CommonMetaFeatureExtractor, data: Instances): String = select(extractor, data, transformerPerformanceMap)
}
