package com.warrior.classification_workflow.core.meta.features

import com.warrior.classification_workflow.core.meta.features.general.*
import com.warrior.classification_workflow.core.meta.features.informationtheoretic.*
import com.warrior.classification_workflow.core.meta.features.statistical.*
import weka.core.Attribute
import weka.core.Instances
import java.util.*

/**
 * Created by warrior on 11/18/16.
 */
class MetaFeatureExtractor(instances: Instances) {

    private val dataSetDimensionality: DataSetDimensionality = DataSetDimensionality()
    private val numberOfClasses: NumberOfClasses = NumberOfClasses()
    private val numberOfFeatures: NumberOfFeatures = NumberOfFeatures()
    private val numberOfInstances: NumberOfInstances = NumberOfInstances()
    private val meanCoefficientOfVariation: MeanCoefficientOfVariation = MeanCoefficientOfVariation()
    private val meanKurtosis: MeanKurtosis = MeanKurtosis()
    private val meanSkewness: MeanSkewness = MeanSkewness()
    private val meanStandardDeviation: MeanStandardDeviation = MeanStandardDeviation()
    private val equivalentNumberOfFeatures: EquivalentNumberOfFeatures = EquivalentNumberOfFeatures()
    private val maxMutualInformation: MaxMutualInformation = MaxMutualInformation()
    private val meanMutualInformation: MeanMutualInformation = MeanMutualInformation()
    private val meanNormalizedFeatureEntropy: MeanNormalizedFeatureEntropy = MeanNormalizedFeatureEntropy()
    private val noiseSignalRatio: NoiseSignalRatio = NoiseSignalRatio()
    private val normalizedClassEntropy: NormalizedClassEntropy = NormalizedClassEntropy()

    private val metaFeatures: List<AbstractMetaFeature> = listOf(
            dataSetDimensionality,
            numberOfClasses,
            numberOfFeatures,
            numberOfInstances,
            meanCoefficientOfVariation,
            meanKurtosis,
            meanSkewness,
            meanStandardDeviation,
            equivalentNumberOfFeatures,
            maxMutualInformation,
            meanMutualInformation,
            meanNormalizedFeatureEntropy,
            noiseSignalRatio,
            normalizedClassEntropy
    )

    init {
        for (metaFeature in metaFeatures) {
            metaFeature.instances = instances
        }

        val mutualInformationCache = HashMap<Attribute, Double>()
        equivalentNumberOfFeatures.setMutualInformationCache(mutualInformationCache)
        maxMutualInformation.setMutualInformationCache(mutualInformationCache)
        meanMutualInformation.setMutualInformationCache(mutualInformationCache)
        noiseSignalRatio.setMutualInformationCache(mutualInformationCache)

        val entropyCache = HashMap<Attribute, EntropyResult>()
        meanNormalizedFeatureEntropy.setEntropyCache(entropyCache)
        noiseSignalRatio.setEntropyCache(entropyCache)
    }

    fun extract(): Result = Result(
            datasetDimensionality = dataSetDimensionality.compute(),
            numberOfClasses = numberOfClasses.compute(),
            numberOfFeatures = numberOfFeatures.compute(),
            numberOfInstances = numberOfInstances.compute(),
            meanCoefficientOfVariation = meanCoefficientOfVariation.compute(),
            meanKurtosis = meanKurtosis.compute(),
            meanSkewness = meanSkewness.compute(),
            meanStandardDeviation = meanStandardDeviation.compute(),
            equivalentNumberOfFeatures = equivalentNumberOfFeatures.compute(),
            maxMutualInformation = maxMutualInformation.compute(),
            meanMutualInformation = meanMutualInformation.compute(),
            meanNormalizedFeatureEntropy = meanNormalizedFeatureEntropy.compute(),
            normalizedClassEntropy = normalizedClassEntropy.compute(),
            noiseSignalRatio = noiseSignalRatio.compute()
    )

    data class Result(
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
}
