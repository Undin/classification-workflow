package com.warrior.classification_workflow.meta_learning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

/**
 * Created by warrior on 11/21/16.
 */
@Entity
@Table(name = "classifier_performance", schema = "public", catalog = "master")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassifierPerformanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "classifier_performance_seq")
    @SequenceGenerator(name = "classifier_performance_seq", sequenceName = "classifier_performance_seq")
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "classifier_name", nullable = false)
    @JsonProperty("classifier_name")
    private String classifierName;

    @Column(name = "dataset_name", nullable = false)
    @JsonProperty("dataset_name")
    private String datasetName;

    @Column(name = "measure", nullable = false)
    @JsonProperty("measure")
    private double measure;

    public ClassifierPerformanceEntity() {}

    public ClassifierPerformanceEntity(String classifierName, String datasetName, double measure) {
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

        ClassifierPerformanceEntity that = (ClassifierPerformanceEntity) o;

        if (id != that.id) return false;
        if (Double.compare(that.measure, measure) != 0) return false;
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
        result = 31 * result + (classifierName != null ? classifierName.hashCode() : 0);
        result = 31 * result + (datasetName != null ? datasetName.hashCode() : 0);
        temp = Double.doubleToLongBits(measure);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
