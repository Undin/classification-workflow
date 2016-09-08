package com.warrior.classification.workflow.baseline

import javax.persistence.*

/**
 * Created by warrior on 07/09/16.
 */
@Entity
@Table(name = "results", schema = "public", catalog = "workflow-results")
class ResultEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int = 0

    @Basic
    @Column(name = "dataset_name")
    var datasetName: String? = null

    @Basic
    @Column(name = "algorithm")
    var algorithm: String? = null

    @Basic
    @Column(name = "measure")
    var measure: String? = null

    @Basic
    @Column(name = "value")
    var value: Double = 0.toDouble()

    constructor(datasetName: String, algorithm: String, measure: String, value: Double) : this() {
        this.datasetName = datasetName
        this.algorithm = algorithm
        this.measure = measure
        this.value = value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as ResultEntity

        if (id != that.id) return false
        if (java.lang.Double.compare(that.value, value) != 0) return false
        if (if (datasetName != null) datasetName != that.datasetName else that.datasetName != null) return false
        if (if (algorithm != null) algorithm != that.algorithm else that.algorithm != null) return false
        if (if (measure != null) measure != that.measure else that.measure != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result: Int
        val temp: Long
        result = id
        result = 31 * result + if (datasetName != null) datasetName!!.hashCode() else 0
        result = 31 * result + if (algorithm != null) algorithm!!.hashCode() else 0
        result = 31 * result + if (measure != null) measure!!.hashCode() else 0
        temp = java.lang.Double.doubleToLongBits(value)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        return result
    }
}
