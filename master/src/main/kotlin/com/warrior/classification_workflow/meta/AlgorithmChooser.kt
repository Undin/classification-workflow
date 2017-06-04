package com.warrior.classification_workflow.meta

import com.warrior.classification_workflow.core.meta.entity.ClassifierPerformanceEntity
import com.warrior.classification_workflow.core.meta.entity.MetaFeaturesEntity
import com.warrior.classification_workflow.core.meta.entity.TransformerPerformanceEntity
import com.warrior.classification_workflow.core.meta.features.CommonMetaFeatureExtractor
import weka.core.Instances
import java.util.*

/**
 * Created by warrior on 11/29/16.
 */
open class AlgorithmChooser(
        private val datasets: Set<String>,
        private val classifiers: Set<String>,
        private val transformers: Set<String>,
        metaFeatures: List<MetaFeaturesEntity>,
        classifierPerformance: List<ClassifierPerformanceEntity>,
        transformerPerformance: List<TransformerPerformanceEntity>,
        private val selector: Selector = SoftMaxSelector()
) {

    private val NEAREST_NEIGHBORS = 5

    // [dataset x meta-features array]
    private val metaFeaturesList: List<Pair<String, DoubleArray>>
    private val metaFeaturesMinMax: Array<MinMax>
    // classifier -> dataset -> value
    protected val classifierPerformanceMap: Map<String, Map<String, Double>>
    // transformer -> dataset -> value
    protected val transformerPerformanceMap: Map<String, Map<String, Double>>
    // algorithm -> dataset -> value
    protected val algorithmsPerformanceMap: Map<String, Map<String, Double>>

    init {
        metaFeatures.requireNotEmpty("meta feature list")
        classifierPerformance.requireNotEmpty("classifier performance list")
        transformerPerformance.requireNotEmpty("transformer performance list")

        val res = createMetaFeatures(metaFeatures)
        metaFeaturesList = res.first
        metaFeaturesMinMax = res.second
        classifierPerformanceMap = createClassifierPerformanceMap(classifierPerformance)
        transformerPerformanceMap = createTransformerPerformanceMap(transformerPerformance)
        algorithmsPerformanceMap = HashMap()
        algorithmsPerformanceMap += classifierPerformanceMap
        algorithmsPerformanceMap += transformerPerformanceMap
    }

    private fun createMetaFeatures(metaFeatures: List<MetaFeaturesEntity>): Pair<List<Pair<String, DoubleArray>>, Array<MinMax>> {
        val metaFeaturesList = ArrayList<Pair<String, DoubleArray>>()
        val metaFeature = metaFeatures[0].toDoubleArray()
        val metaFeaturesMinMax = Array(metaFeature.size) { i -> MinMax(metaFeature[i], metaFeature[i]) }
        for (m in metaFeatures) {
            if (m.datasetName in datasets) {
                val values = m.toDoubleArray()
                metaFeaturesList += Pair(m.datasetName, values)
                for ((i, v) in values.withIndex()) {
                    val (min, max) = metaFeaturesMinMax[i]
                    metaFeaturesMinMax[i] = MinMax(Math.min(min, v), Math.max(max, v))
                }
            }
        }
        return Pair(metaFeaturesList, metaFeaturesMinMax)
    }

    private fun createClassifierPerformanceMap(classifierPerformance: List<ClassifierPerformanceEntity>): Map<String, Map<String, Double>> {
        val classifierPerformanceMap = HashMap<String, MutableMap<String, Double>>()
        for ((datasetName, classifierName, measure) in classifierPerformance) {
            if (datasetName in datasets && classifierName in classifiers) {
                val classifierMap = classifierPerformanceMap.getOrPut(classifierName) { HashMap() }
                classifierMap[datasetName] = measure
            }
        }
        return classifierPerformanceMap
    }

    private fun createTransformerPerformanceMap(transformerPerformance: List<TransformerPerformanceEntity>): Map<String, Map<String, Double>> {
        val transformerPerformanceMap = HashMap<String, MutableMap<String, Double>>()
        val countMap = HashMap<String, MutableMap<String, Int>>()
        for ((datasetName, transformerName, classifierName, value) in transformerPerformance) {
            if (datasetName in datasets && transformerName in transformers && classifierName in classifiers) {
                val transformerMap = transformerPerformanceMap.getOrPut(transformerName) { HashMap() }
                val countTransformerMap = countMap.getOrPut(transformerName) { HashMap() }
                transformerMap.merge(datasetName, value) { old, new -> old + new }
                countTransformerMap.merge(datasetName, 1) { old, new -> old + new }
            }
        }
        for (transformer in transformers) {
            val transformerMap = transformerPerformanceMap[transformer]
            val transformerCountMap = countMap[transformer]
            if (transformerMap != null && transformerCountMap != null) {
                for (dataset in datasets) {
                    val count = transformerCountMap[dataset]
                    if (count != null) {
                        transformerMap.computeIfPresent(dataset) { key, value -> value / count }
                    }
                }
            }
        }
        return transformerPerformanceMap
    }

    open fun chooseAlgorithm(extractor: CommonMetaFeatureExtractor, data: Instances): String = select(extractor, data, algorithmsPerformanceMap)

    open fun chooseClassifier(extractor: CommonMetaFeatureExtractor, data: Instances): String = select(extractor, data, classifierPerformanceMap)

    protected fun select(extractor: CommonMetaFeatureExtractor, data: Instances, performanceMap: Map<String, Map<String, Double>>): String {
        val metaFeatures = extractor.extract(data).toDoubleArray()
        val nearestDatasets = findNearests(metaFeatures)
        val revertDistanceSum = nearestDatasets.map { 1 / it.distance }.sum()

        val estimationMap = HashMap<String, Double>()
        for (algorithm in performanceMap.keys) {
            val algorithmMap = performanceMap[algorithm] ?: throw IllegalStateException()
            val performanceEstimation = nearestDatasets.map { dataset ->
                val perf = algorithmMap[dataset.name] ?: 0.0
                perf / dataset.distance
            }.sum() / revertDistanceSum
            estimationMap[algorithm] = performanceEstimation
        }

        return selector.select(estimationMap)
    }

    private fun findNearests(metaFeatures: DoubleArray): List<DatasetDistance> {
        val minMax = metaFeaturesMinMax.zip(metaFeatures.toTypedArray())
                .map { v ->
                    val (min, max) = v.first
                    val value = v.second
                    MinMax(Math.min(min, value), Math.max(max, value))
                }
        val distances = metaFeaturesList.asSequence()
                .map { m ->
                    val (name, values) = m
                    val sum = metaFeatures.mapIndexed { i, value ->
                        val (min, max) = minMax[i]
                        val sub = (value - values[i]) / (max - min)
                        sub * sub
                    }.sum()
                    DatasetDistance(name, Math.sqrt(sum))
                }.sortedBy { it.distance }
                .take(NEAREST_NEIGHBORS)
                .toList()
        return distances
    }

    private fun <T> List<T>.requireNotEmpty(name: String = "") {
        if (isEmpty()) {
            throw IllegalArgumentException("$name must be not empty")
        }
    }

    private data class MinMax(val min: Double, val max: Double)
    private data class DatasetDistance(val name: String, val distance: Double)
}
