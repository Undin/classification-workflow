package com.warrior.classification_workflow.core.meta.features

/**
 * Created by warrior on 12/1/16.
 */
data class MetaFeatures(
        val numberOfInstances: Double,
        val numberOfFeatures: Double,
        val numberOfClasses: Double,
        val datasetDimensionality: Double,
        val meanCoefficientOfVariation: Double,
        val meanKurtosis: Double,
        val meanSkewness: Double,
        val meanStandardDeviation: Double,
        val equivalentNumberOfFeatures: Double,
        val maxMutualInformation: Double,
        val meanMutualInformation: Double,
        val meanNormalizedFeatureEntropy: Double,
        val noiseSignalRatio: Double,
        val normalizedClassEntropy: Double
)
