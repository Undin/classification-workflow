package com.warrior.classification_workflow.core.meta.features

import com.warrior.classification_workflow.core.meta.entity.MetaFeaturesEntity
import com.warrior.classification_workflow.core.meta.features.general.*
import com.warrior.classification_workflow.core.meta.features.informationtheoretic.*
import com.warrior.classification_workflow.core.meta.features.statistical.*
import weka.core.Attribute
import weka.core.Instances
import java.util.*

/**
 * Created by warrior on 11/18/16.
 */
class CommonMetaFeatureExtractor() {

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

    init {
        val mutualInformationCache = HashMap<Attribute, Double>()
        equivalentNumberOfFeatures.setMutualInformationCache(mutualInformationCache)
        maxMutualInformation.setMutualInformationCache(mutualInformationCache)
        meanMutualInformation.setMutualInformationCache(mutualInformationCache)
        noiseSignalRatio.setMutualInformationCache(mutualInformationCache)

        val entropyCache = HashMap<Attribute, EntropyResult>()
        meanNormalizedFeatureEntropy.setEntropyCache(entropyCache)
        noiseSignalRatio.setEntropyCache(entropyCache)
    }

    fun extract(instances: Instances): MetaFeaturesEntity = MetaFeaturesEntity(
            datasetName = instances.relationName(),
            datasetDimensionality = dataSetDimensionality.compute(instances),
            numberOfClasses = numberOfClasses.compute(instances),
            numberOfFeatures = numberOfFeatures.compute(instances),
            numberOfInstances = numberOfInstances.compute(instances),
            meanCoefficientOfVariation = meanCoefficientOfVariation.compute(instances),
            meanKurtosis = meanKurtosis.compute(instances),
            meanSkewness = meanSkewness.compute(instances),
            meanStandardDeviation = meanStandardDeviation.compute(instances),
            equivalentNumberOfFeatures = equivalentNumberOfFeatures.compute(instances),
            maxMutualInformation = maxMutualInformation.compute(instances),
            meanMutualInformation = meanMutualInformation.compute(instances),
            meanNormalizedFeatureEntropy = meanNormalizedFeatureEntropy.compute(instances),
            normalizedClassEntropy = normalizedClassEntropy.compute(instances),
            noiseSignalRatio = noiseSignalRatio.compute(instances)
    )
}
