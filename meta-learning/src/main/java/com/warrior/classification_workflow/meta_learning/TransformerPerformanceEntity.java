package com.warrior.classification_workflow.meta_learning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

/**
 * Created by warrior on 11/25/16.
 */
@Entity
@Table(name = "transformer_performance", schema = "public", catalog = "master")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformerPerformanceEntity {
    private int id;
    private String transformerName;
    private String classifierName;
    private String datasetName;
    private double measure;

    public TransformerPerformanceEntity() {}

    public TransformerPerformanceEntity(String transformerName, String classifierName, String datasetName, double measure) {
        this.transformerName = transformerName;
        this.classifierName = classifierName;
        this.datasetName = datasetName;
        this.measure = measure;
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
    @Column(name = "transformer_name")
    @JsonProperty("transformer_name")
    public String getTransformerName() {
        return transformerName;
    }

    @JsonProperty("transformer_name")
    public void setTransformerName(String transformerName) {
        this.transformerName = transformerName;
    }

    @Basic
    @Column(name = "classifier_name")
    @JsonProperty("classifier_name")
    public String getClassifierName() {
        return classifierName;
    }

    @JsonProperty("classifier_name")
    public void setClassifierName(String classifierName) {
        this.classifierName = classifierName;
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
    @Column(name = "measure")
    @JsonProperty("measure")
    public double getMeasure() {
        return measure;
    }

    @JsonProperty("measure")
    public void setMeasure(double measure) {
        this.measure = measure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransformerPerformanceEntity that = (TransformerPerformanceEntity) o;

        if (id != that.id) return false;
        if (Double.compare(that.measure, measure) != 0) return false;
        if (transformerName != null ? !transformerName.equals(that.transformerName) : that.transformerName != null)
            return false;
        if (classifierName != null ? !classifierName.equals(that.classifierName) : that.classifierName != null)
            return false;
        if (datasetName != null ? !datasetName.equals(that.datasetName) : that.datasetName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (transformerName != null ? transformerName.hashCode() : 0);
        result = 31 * result + (classifierName != null ? classifierName.hashCode() : 0);
        result = 31 * result + (datasetName != null ? datasetName.hashCode() : 0);
        temp = Double.doubleToLongBits(measure);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
