package com.warrior.classification_workflow.meta_learning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.warrior.classification_workflow.meta_learning.metafeatures.MetaFeatureExtractor;

import javax.persistence.*;

/**
 * Created by warrior on 11/17/16.
 */
@Entity
@Table(name = "meta_features", schema = "public", catalog = "master")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaFeaturesEntity {
    private int id;
    private String datasetName;
    private double numberOfInstances;
    private double numberOfFeatures;
    private double numberOfClasses;
    private double datasetDimensionality;
    private double meanCoefficientOfVariation;
    private double meanKurtosis;
    private double meanSkewness;
    private double meanStandardDeviation;
    private double equivalentNumberOfFeatures;
    private double maxMutualInformation;
    private double meanMutualInformation;
    private double meanNormalizedFeatureEntropy;
    private double noiseSignalRatio;
    private double normalizedClassEntropy;

    public MetaFeaturesEntity() {}

    public MetaFeaturesEntity(String name, MetaFeatureExtractor.Result result) {
        datasetName = name;
        numberOfInstances = result.getNumberOfInstances();
        numberOfFeatures = result.getNumberOfFeatures();
        numberOfClasses = result.getNumberOfClasses();
        datasetDimensionality = result.getDatasetDimensionality();
        meanCoefficientOfVariation = result.getMeanCoefficientOfVariation();
        meanKurtosis = result.getMeanKurtosis();
        meanSkewness = result.getMeanSkewness();
        meanStandardDeviation = result.getMeanStandardDeviation();
        equivalentNumberOfFeatures = result.getEquivalentNumberOfFeatures();
        maxMutualInformation = result.getMaxMutualInformation();
        meanMutualInformation = result.getMeanMutualInformation();
        meanNormalizedFeatureEntropy = result.getMeanNormalizedFeatureEntropy();
        noiseSignalRatio = result.getNoiseSignalRatio();
        normalizedClassEntropy = result.getNormalizedClassEntropy();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "dataset_name")
    @JsonProperty("dataset_name")
    public String getDatasetName() {
        return datasetName;
    }

    @JsonProperty("dataset_name")
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    @Basic
    @Column(name = "number_of_instances")
    @JsonProperty("number_of_instances")
    public double getNumberOfInstances() {
        return numberOfInstances;
    }

    @JsonProperty("number_of_instances")
    public void setNumberOfInstances(double numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    @Basic
    @Column(name = "number_of_features")
    @JsonProperty("number_of_features")
    public double getNumberOfFeatures() {
        return numberOfFeatures;
    }

    @JsonProperty("number_of_features")
    public void setNumberOfFeatures(double numberOfFeatures) {
        this.numberOfFeatures = numberOfFeatures;
    }

    @Basic
    @Column(name = "number_of_classes")
    @JsonProperty("number_of_classes")
    public double getNumberOfClasses() {
        return numberOfClasses;
    }

    @JsonProperty("number_of_classes")
    public void setNumberOfClasses(double numberOfClasses) {
        this.numberOfClasses = numberOfClasses;
    }

    @Basic
    @Column(name = "dataset_dimensionality")
    @JsonProperty("dataset_dimensionality")
    public double getDatasetDimensionality() {
        return datasetDimensionality;
    }

    @JsonProperty("dataset_dimensionality")
    public void setDatasetDimensionality(double datasetDimensionality) {
        this.datasetDimensionality = datasetDimensionality;
    }

    @Basic
    @Column(name = "mean_coefficient_of_variation")
    @JsonProperty("mean_coefficient_of_variation")
    public double getMeanCoefficientOfVariation() {
        return meanCoefficientOfVariation;
    }

    @JsonProperty("mean_coefficient_of_variation")
    public void setMeanCoefficientOfVariation(double meanCoefficientOfVariation) {
        this.meanCoefficientOfVariation = meanCoefficientOfVariation;
    }

    @Basic
    @Column(name = "mean_kurtosis")
    @JsonProperty("mean_kurtosis")
    public double getMeanKurtosis() {
        return meanKurtosis;
    }

    @JsonProperty("mean_kurtosis")
    public void setMeanKurtosis(double meanKurtosis) {
        this.meanKurtosis = meanKurtosis;
    }

    @Basic
    @Column(name = "mean_skewness")
    @JsonProperty("mean_skewness")
    public double getMeanSkewness() {
        return meanSkewness;
    }

    @JsonProperty("mean_skewness")
    public void setMeanSkewness(double meanSkewness) {
        this.meanSkewness = meanSkewness;
    }

    @Basic
    @Column(name = "mean_standard_deviation")
    @JsonProperty("mean_standard_deviation")
    public double getMeanStandardDeviation() {
        return meanStandardDeviation;
    }

    @JsonProperty("mean_standard_deviation")
    public void setMeanStandardDeviation(double meanStandardDeviation) {
        this.meanStandardDeviation = meanStandardDeviation;
    }

    @Basic
    @Column(name = "equivalent_number_of_features")
    @JsonProperty("equivalent_number_of_features")
    public double getEquivalentNumberOfFeatures() {
        return equivalentNumberOfFeatures;
    }

    @JsonProperty("equivalent_number_of_features")
    public void setEquivalentNumberOfFeatures(double equivalentNumberOfFeatures) {
        this.equivalentNumberOfFeatures = equivalentNumberOfFeatures;
    }

    @Basic
    @Column(name = "max_mutual_information")
    @JsonProperty("max_mutual_information")
    public double getMaxMutualInformation() {
        return maxMutualInformation;
    }

    @JsonProperty("max_mutual_information")
    public void setMaxMutualInformation(double maxMutualInformation) {
        this.maxMutualInformation = maxMutualInformation;
    }

    @Basic
    @Column(name = "mean_mutual_information")
    @JsonProperty("mean_mutual_information")
    public double getMeanMutualInformation() {
        return meanMutualInformation;
    }

    @JsonProperty("mean_mutual_information")
    public void setMeanMutualInformation(double meanMutualInformation) {
        this.meanMutualInformation = meanMutualInformation;
    }

    @Basic
    @Column(name = "mean_normalized_feature_entropy")
    @JsonProperty("mean_normalized_feature_entropy")
    public double getMeanNormalizedFeatureEntropy() {
        return meanNormalizedFeatureEntropy;
    }

    @JsonProperty("mean_normalized_feature_entropy")
    public void setMeanNormalizedFeatureEntropy(double meanNormalizedFeatureEntropy) {
        this.meanNormalizedFeatureEntropy = meanNormalizedFeatureEntropy;
    }

    @Basic
    @Column(name = "noise_signal_ratio")
    @JsonProperty("noise_signal_ratio")
    public double getNoiseSignalRatio() {
        return noiseSignalRatio;
    }

    @JsonProperty("noise_signal_ratio")
    public void setNoiseSignalRatio(double noiseSignalRatio) {
        this.noiseSignalRatio = noiseSignalRatio;
    }

    @Basic
    @Column(name = "normalized_class_entropy")
    @JsonProperty("normalized_class_entropy")
    public double getNormalizedClassEntropy() {
        return normalizedClassEntropy;
    }

    @JsonProperty("normalized_class_entropy")
    public void setNormalizedClassEntropy(double normalizedClassEntropy) {
        this.normalizedClassEntropy = normalizedClassEntropy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetaFeaturesEntity that = (MetaFeaturesEntity) o;

        if (id != that.id) return false;
        if (Double.compare(that.numberOfInstances, numberOfInstances) != 0) return false;
        if (Double.compare(that.numberOfFeatures, numberOfFeatures) != 0) return false;
        if (Double.compare(that.numberOfClasses, numberOfClasses) != 0) return false;
        if (Double.compare(that.datasetDimensionality, datasetDimensionality) != 0) return false;
        if (Double.compare(that.meanCoefficientOfVariation, meanCoefficientOfVariation) != 0) return false;
        if (Double.compare(that.meanKurtosis, meanKurtosis) != 0) return false;
        if (Double.compare(that.meanSkewness, meanSkewness) != 0) return false;
        if (Double.compare(that.meanStandardDeviation, meanStandardDeviation) != 0) return false;
        if (Double.compare(that.equivalentNumberOfFeatures, equivalentNumberOfFeatures) != 0) return false;
        if (Double.compare(that.maxMutualInformation, maxMutualInformation) != 0) return false;
        if (Double.compare(that.meanMutualInformation, meanMutualInformation) != 0) return false;
        if (Double.compare(that.meanNormalizedFeatureEntropy, meanNormalizedFeatureEntropy) != 0) return false;
        if (Double.compare(that.noiseSignalRatio, noiseSignalRatio) != 0) return false;
        if (Double.compare(that.normalizedClassEntropy, normalizedClassEntropy) != 0) return false;
        if (datasetName != null ? !datasetName.equals(that.datasetName) : that.datasetName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (datasetName != null ? datasetName.hashCode() : 0);
        temp = Double.doubleToLongBits(numberOfInstances);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(numberOfFeatures);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(numberOfClasses);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(datasetDimensionality);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(meanCoefficientOfVariation);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(meanKurtosis);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(meanSkewness);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(meanStandardDeviation);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(equivalentNumberOfFeatures);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxMutualInformation);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(meanMutualInformation);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(meanNormalizedFeatureEntropy);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(noiseSignalRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(normalizedClassEntropy);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
