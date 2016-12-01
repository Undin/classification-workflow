package com.warrior.classification_workflow.meta_learning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.warrior.classification_workflow.core.meta.features.MetaFeatureExtractor;

import javax.persistence.*;

/**
 * Created by warrior on 11/17/16.
 */
@Entity
@Table(name = "meta_features", schema = "public", catalog = "master")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaFeaturesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meta_features_seq")
    @SequenceGenerator(name = "meta_features_seq", sequenceName = "meta_features_seq")
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "dataset_name", nullable = false)
    @JsonProperty("dataset_name")
    private String datasetName;

    @Column(name = "number_of_instances", nullable = false)
    @JsonProperty("number_of_instances")
    private double numberOfInstances;

    @Column(name = "number_of_features", nullable = false)
    @JsonProperty("number_of_features")
    private double numberOfFeatures;

    @Column(name = "number_of_classes", nullable = false)
    @JsonProperty("number_of_classes")
    private double numberOfClasses;

    @Column(name = "dataset_dimensionality", nullable = false)
    @JsonProperty("dataset_dimensionality")
    private double datasetDimensionality;

    @Column(name = "mean_coefficient_of_variation", nullable = false)
    @JsonProperty("mean_coefficient_of_variation")
    private double meanCoefficientOfVariation;

    @Column(name = "mean_kurtosis", nullable = false)
    @JsonProperty("mean_kurtosis")
    private double meanKurtosis;

    @Basic
    @Column(name = "mean_skewness", nullable = false)
    @JsonProperty("mean_skewness")
    private double meanSkewness;

    @Column(name = "mean_standard_deviation", nullable = false)
    @JsonProperty("mean_standard_deviation")
    private double meanStandardDeviation;
    
    @Column(name = "equivalent_number_of_features", nullable = false)
    @JsonProperty("equivalent_number_of_features")
    private double equivalentNumberOfFeatures;

    @Column(name = "max_mutual_information", nullable = false)
    @JsonProperty("max_mutual_information")
    private double maxMutualInformation;

    @Column(name = "mean_mutual_information", nullable = false)
    @JsonProperty("mean_mutual_information")
    private double meanMutualInformation;

    @Column(name = "mean_normalized_feature_entropy", nullable = false)
    @JsonProperty("mean_normalized_feature_entropy")
    private double meanNormalizedFeatureEntropy;

    @Column(name = "noise_signal_ratio", nullable = false)
    @JsonProperty("noise_signal_ratio")
    private double noiseSignalRatio;
    
    @Column(name = "normalized_class_entropy", nullable = false)
    @JsonProperty("normalized_class_entropy")
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public double getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(double numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public double getNumberOfFeatures() {
        return numberOfFeatures;
    }

    public void setNumberOfFeatures(double numberOfFeatures) {
        this.numberOfFeatures = numberOfFeatures;
    }

    public double getNumberOfClasses() {
        return numberOfClasses;
    }

    public void setNumberOfClasses(double numberOfClasses) {
        this.numberOfClasses = numberOfClasses;
    }

    public double getDatasetDimensionality() {
        return datasetDimensionality;
    }

    public void setDatasetDimensionality(double datasetDimensionality) {
        this.datasetDimensionality = datasetDimensionality;
    }

    public double getMeanCoefficientOfVariation() {
        return meanCoefficientOfVariation;
    }

    public void setMeanCoefficientOfVariation(double meanCoefficientOfVariation) {
        this.meanCoefficientOfVariation = meanCoefficientOfVariation;
    }

    public double getMeanKurtosis() {
        return meanKurtosis;
    }

    public void setMeanKurtosis(double meanKurtosis) {
        this.meanKurtosis = meanKurtosis;
    }

    public double getMeanSkewness() {
        return meanSkewness;
    }

    public void setMeanSkewness(double meanSkewness) {
        this.meanSkewness = meanSkewness;
    }

    public double getMeanStandardDeviation() {
        return meanStandardDeviation;
    }

    public void setMeanStandardDeviation(double meanStandardDeviation) {
        this.meanStandardDeviation = meanStandardDeviation;
    }

    public double getEquivalentNumberOfFeatures() {
        return equivalentNumberOfFeatures;
    }

    public void setEquivalentNumberOfFeatures(double equivalentNumberOfFeatures) {
        this.equivalentNumberOfFeatures = equivalentNumberOfFeatures;
    }
    
    public double getMaxMutualInformation() {
        return maxMutualInformation;
    }

    public void setMaxMutualInformation(double maxMutualInformation) {
        this.maxMutualInformation = maxMutualInformation;
    }

    public double getMeanMutualInformation() {
        return meanMutualInformation;
    }

    public void setMeanMutualInformation(double meanMutualInformation) {
        this.meanMutualInformation = meanMutualInformation;
    }

    public double getMeanNormalizedFeatureEntropy() {
        return meanNormalizedFeatureEntropy;
    }

    public void setMeanNormalizedFeatureEntropy(double meanNormalizedFeatureEntropy) {
        this.meanNormalizedFeatureEntropy = meanNormalizedFeatureEntropy;
    }
    
    public double getNoiseSignalRatio() {
        return noiseSignalRatio;
    }

    public void setNoiseSignalRatio(double noiseSignalRatio) {
        this.noiseSignalRatio = noiseSignalRatio;
    }

    public double getNormalizedClassEntropy() {
        return normalizedClassEntropy;
    }

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
