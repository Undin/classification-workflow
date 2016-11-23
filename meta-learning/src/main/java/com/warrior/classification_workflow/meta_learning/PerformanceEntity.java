package com.warrior.classification_workflow.meta_learning;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

/**
 * Created by warrior on 11/21/16.
 */
@Entity
@Table(name = "performance", schema = "public", catalog = "master")
public class PerformanceEntity {
    private int id;
    private String classifierName;
    private String datasetName;
    private double measure;

    public PerformanceEntity() {}

    public PerformanceEntity(String classifierName, String datasetName, double measure) {
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

        PerformanceEntity that = (PerformanceEntity) o;

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
