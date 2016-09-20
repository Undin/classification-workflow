package com.warrior.classification.workflow.baseline

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.warrior.classification.workflow.baseline.json.JsonBinaryType
import com.warrior.classification.workflow.core.Workflow
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import javax.persistence.*

/**
 * Created by warrior on 07/09/16.
 */
@TypeDefs(TypeDef(name = "jsonb", typeClass = JsonBinaryType::class))
@Entity
@Table(name = "results", schema = "public", catalog = "workflow-results")
class ResultEntity() {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    var id: Int = 0

    @JsonProperty("dataset_name")
    @Basic
    @Column(name = "dataset_name")
    var datasetName: String? = null

    @JsonProperty("workflow")
    @Type(type = "jsonb")
    @Column(name = "workflow", columnDefinition = "json")
    var workflow: Workflow? = null

    @JsonProperty("measure")
    @Basic
    @Column(name = "measure")
    var measure: String? = null

    @JsonProperty("value")
    @Basic
    @Column(name = "value")
    var value: Double = 0.toDouble()

    constructor(datasetName: String, workflow: Workflow, measure: String, value: Double) : this() {
        this.datasetName = datasetName
        this.workflow = workflow
        this.measure = measure
        this.value = value
    }

    @JsonCreator
    constructor(@JsonProperty("id") id: Int, @JsonProperty("dataset_name") datasetName: String,
                @JsonProperty("workflow") workflow: Workflow,
                @JsonProperty("measure") measure: String, @JsonProperty("value") value: Double) : this() {
        this.datasetName = datasetName
        this.workflow = workflow
        this.measure = measure
        this.value = value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ResultEntity

        if (id != other.id) return false
        if (datasetName != other.datasetName) return false
        if (workflow != other.workflow) return false
        if (measure != other.measure) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (datasetName?.hashCode() ?: 0)
        result = 31 * result + (workflow?.hashCode() ?: 0)
        result = 31 * result + (measure?.hashCode() ?: 0)
        result = 31 * result + value.hashCode()
        return result
    }
}
