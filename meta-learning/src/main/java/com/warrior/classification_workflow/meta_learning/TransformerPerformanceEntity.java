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

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transformer_performance_seq")
    @SequenceGenerator(name = "transformer_performance_seq", sequenceName = "transformer_performance_seq")
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "transformer_name", nullable = false)
    @JsonProperty("transformer_name")
    private String transformerName;

    @Column(name = "classifier_name", nullable = false)
    @JsonProperty("classifier_name")
    private String classifierName;

    @Column(name = "dataset_name", nullable = false)
    @JsonProperty("dataset_name")
    private String datasetName;

    @Column(name = "measure", nullable = false)
    @JsonProperty("measure")
    private double measure;

    public TransformerPerformanceEntity() {}

    public TransformerPerformanceEntity(String transformerName, String classifierName, String datasetName, double measure) {
        this.transformerName = transformerName;
        this.classifierName = classifierName;
        this.datasetName = datasetName;
        this.measure = measure;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransformerName() {
        return transformerName;
    }

    public void setTransformerName(String transformerName) {
        this.transformerName = transformerName;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public void setClassifierName(String classifierName) {
        this.classifierName = classifierName;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public double getMeasure() {
        return measure;
    }

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
