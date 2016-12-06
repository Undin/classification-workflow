package com.warrior.classification_workflow.core.meta.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by warrior on 12/1/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MetaFeaturesEntity(
        @JsonProperty("dataset_name") val datasetName: String,
        @JsonProperty("number_of_instances") val numberOfInstances: Double,
        @JsonProperty("number_of_features") val numberOfFeatures: Double,
        @JsonProperty("number_of_classes") val numberOfClasses: Double,
        @JsonProperty("dataset_dimensionality") val datasetDimensionality: Double,
        @JsonProperty("mean_coefficient_of_variation") val meanCoefficientOfVariation: Double,
        @JsonProperty("mean_kurtosis") val meanKurtosis: Double,
        @JsonProperty("mean_skewness") val meanSkewness: Double,
        @JsonProperty("mean_standard_deviation") val meanStandardDeviation: Double,
        @JsonProperty("equivalent_number_of_features") val equivalentNumberOfFeatures: Double,
        @JsonProperty("max_mutual_information") val maxMutualInformation: Double,
        @JsonProperty("mean_mutual_information") val meanMutualInformation: Double,
        @JsonProperty("mean_normalized_feature_entropy") val meanNormalizedFeatureEntropy: Double,
        @JsonProperty("noise_signal_ratio") val noiseSignalRatio: Double,
        @JsonProperty("normalized_class_entropy") val normalizedClassEntropy: Double
) {
    fun toDoubleArray(): DoubleArray {
        val values = DoubleArray(14)
        values[0] = numberOfInstances
        values[1] = numberOfFeatures
        values[2] = numberOfClasses
        values[3] = datasetDimensionality
        values[4] = meanCoefficientOfVariation
        values[5] = meanKurtosis
        values[6] = meanSkewness
        values[7] = meanStandardDeviation
        values[8] = equivalentNumberOfFeatures
        values[9] = maxMutualInformation
        values[10] = meanMutualInformation
        values[11] = meanNormalizedFeatureEntropy
        values[12] = noiseSignalRatio
        values[13] = normalizedClassEntropy
        return values
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClassifierPerformanceEntity(
        @JsonProperty("dataset_name") val datasetName: String,
        @JsonProperty("classifier_name") val classifierName: String,
        @JsonProperty("measure") val measure: Double
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransformerPerformanceEntity(
        @JsonProperty("dataset_name") var datasetName: String,
        @JsonProperty("transformer_name") val transformerName: String,
        @JsonProperty("classifier_name") var classifierName: String,
        @JsonProperty("measure") var measure: Double
)
