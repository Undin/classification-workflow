package com.warrior.classification_workflow.core.meta.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.*

/**
 * Created by warrior on 11/25/16.
 */
@Entity
@Table(name = "transformer_performance", schema = "public", catalog = "master")
@JsonIgnoreProperties(ignoreUnknown = true)
class TransformerPerformanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transformer_performance_seq")
    @SequenceGenerator(name = "transformer_performance_seq", sequenceName = "transformer_performance_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    @JsonIgnore
    var id: Int = 0

    @Column(name = "transformer_name", nullable = false)
    @JsonProperty("transformer_name")
    lateinit var transformerName: String

    @Column(name = "classifier_name", nullable = false)
    @JsonProperty("classifier_name")
    lateinit var classifierName: String

    @Column(name = "dataset_name", nullable = false)
    @JsonProperty("dataset_name")
    lateinit var datasetName: String

    @Column(name = "measure", nullable = false)
    @JsonProperty("measure")
    var measure: Double = 0.toDouble()

    constructor()

    constructor(transformerName: String, classifierName: String, datasetName: String, measure: Double) {
        this.transformerName = transformerName
        this.classifierName = classifierName
        this.datasetName = datasetName
        this.measure = measure
    }

    operator fun component1(): String = datasetName
    operator fun component2(): String = transformerName
    operator fun component3(): String = classifierName
    operator fun component4(): Double = measure

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TransformerPerformanceEntity

        if (transformerName != other.transformerName) return false
        if (classifierName != other.classifierName) return false
        if (datasetName != other.datasetName) return false
        if (measure != other.measure) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transformerName.hashCode()
        result = 31 * result + classifierName.hashCode()
        result = 31 * result + datasetName.hashCode()
        result = 31 * result + measure.hashCode()
        return result
    }
}
