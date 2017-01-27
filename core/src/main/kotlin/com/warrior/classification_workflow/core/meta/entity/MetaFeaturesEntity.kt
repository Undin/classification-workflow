package com.warrior.classification_workflow.core.meta.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.*

/**
 * Created by warrior on 11/17/16.
 */
@Entity
@Table(name = "meta_features", schema = "public", catalog = "master")
@JsonIgnoreProperties(ignoreUnknown = true)
class MetaFeaturesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meta_features_seq")
    @SequenceGenerator(name = "meta_features_seq", sequenceName = "meta_features_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    @JsonIgnore
    var id: Int = 0

    @Column(name = "dataset_name", nullable = false)
    @JsonProperty("dataset_name")
    lateinit var datasetName: String

    @Column(name = "number_of_instances", nullable = false)
    @JsonProperty("number_of_instances")
    var numberOfInstances: Double = 0.0

    @Column(name = "number_of_features", nullable = false)
    @JsonProperty("number_of_features")
    var numberOfFeatures: Double = 0.0

    @Column(name = "number_of_classes", nullable = false)
    @JsonProperty("number_of_classes")
    var numberOfClasses: Double = 0.0

    @Column(name = "dataset_dimensionality", nullable = false)
    @JsonProperty("dataset_dimensionality")
    var datasetDimensionality: Double = 0.0

    @Column(name = "mean_coefficient_of_variation", nullable = false)
    @JsonProperty("mean_coefficient_of_variation")
    var meanCoefficientOfVariation: Double = 0.0

    @Column(name = "mean_kurtosis", nullable = false)
    @JsonProperty("mean_kurtosis")
    var meanKurtosis: Double = 0.0

    @Basic
    @Column(name = "mean_skewness", nullable = false)
    @JsonProperty("mean_skewness")
    var meanSkewness: Double = 0.0

    @Column(name = "mean_standard_deviation", nullable = false)
    @JsonProperty("mean_standard_deviation")
    var meanStandardDeviation: Double = 0.0

    @Column(name = "equivalent_number_of_features", nullable = false)
    @JsonProperty("equivalent_number_of_features")
    var equivalentNumberOfFeatures: Double = 0.0

    @Column(name = "max_mutual_information", nullable = false)
    @JsonProperty("max_mutual_information")
    var maxMutualInformation: Double = 0.0

    @Column(name = "mean_mutual_information", nullable = false)
    @JsonProperty("mean_mutual_information")
    var meanMutualInformation: Double = 0.0

    @Column(name = "mean_normalized_feature_entropy", nullable = false)
    @JsonProperty("mean_normalized_feature_entropy")
    var meanNormalizedFeatureEntropy: Double = 0.0

    @Column(name = "noise_signal_ratio", nullable = false)
    @JsonProperty("noise_signal_ratio")
    var noiseSignalRatio: Double = 0.0

    @Column(name = "normalized_class_entropy", nullable = false)
    @JsonProperty("normalized_class_entropy")
    var normalizedClassEntropy: Double = 0.0

    constructor()

    constructor(datasetName: String,
                numberOfInstances: Double, numberOfFeatures: Double,
                numberOfClasses: Double, datasetDimensionality: Double,
                meanCoefficientOfVariation: Double, meanKurtosis: Double,
                meanSkewness: Double, meanStandardDeviation: Double,
                equivalentNumberOfFeatures: Double, maxMutualInformation: Double,
                meanMutualInformation: Double, meanNormalizedFeatureEntropy: Double,
                noiseSignalRatio: Double, normalizedClassEntropy: Double) {
        this.datasetName = datasetName
        this.numberOfInstances = numberOfInstances
        this.numberOfFeatures = numberOfFeatures
        this.numberOfClasses = numberOfClasses
        this.datasetDimensionality = datasetDimensionality
        this.meanCoefficientOfVariation = meanCoefficientOfVariation
        this.meanKurtosis = meanKurtosis
        this.meanSkewness = meanSkewness
        this.meanStandardDeviation = meanStandardDeviation
        this.equivalentNumberOfFeatures = equivalentNumberOfFeatures
        this.maxMutualInformation = maxMutualInformation
        this.meanMutualInformation = meanMutualInformation
        this.meanNormalizedFeatureEntropy = meanNormalizedFeatureEntropy
        this.noiseSignalRatio = noiseSignalRatio
        this.normalizedClassEntropy = normalizedClassEntropy
    }

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as MetaFeaturesEntity

        if (datasetName != other.datasetName) return false
        if (numberOfInstances != other.numberOfInstances) return false
        if (numberOfFeatures != other.numberOfFeatures) return false
        if (numberOfClasses != other.numberOfClasses) return false
        if (datasetDimensionality != other.datasetDimensionality) return false
        if (meanCoefficientOfVariation != other.meanCoefficientOfVariation) return false
        if (meanKurtosis != other.meanKurtosis) return false
        if (meanSkewness != other.meanSkewness) return false
        if (meanStandardDeviation != other.meanStandardDeviation) return false
        if (equivalentNumberOfFeatures != other.equivalentNumberOfFeatures) return false
        if (maxMutualInformation != other.maxMutualInformation) return false
        if (meanMutualInformation != other.meanMutualInformation) return false
        if (meanNormalizedFeatureEntropy != other.meanNormalizedFeatureEntropy) return false
        if (noiseSignalRatio != other.noiseSignalRatio) return false
        if (normalizedClassEntropy != other.normalizedClassEntropy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = datasetName.hashCode()
        result = 31 * result + numberOfInstances.hashCode()
        result = 31 * result + numberOfFeatures.hashCode()
        result = 31 * result + numberOfClasses.hashCode()
        result = 31 * result + datasetDimensionality.hashCode()
        result = 31 * result + meanCoefficientOfVariation.hashCode()
        result = 31 * result + meanKurtosis.hashCode()
        result = 31 * result + meanSkewness.hashCode()
        result = 31 * result + meanStandardDeviation.hashCode()
        result = 31 * result + equivalentNumberOfFeatures.hashCode()
        result = 31 * result + maxMutualInformation.hashCode()
        result = 31 * result + meanMutualInformation.hashCode()
        result = 31 * result + meanNormalizedFeatureEntropy.hashCode()
        result = 31 * result + noiseSignalRatio.hashCode()
        result = 31 * result + normalizedClassEntropy.hashCode()
        return result
    }
}
