package com.warrior.classification.workflow.baseline.dataset_info

import javax.persistence.*

/**
 * Created by warrior on 17/09/16.
 */
@Entity
@Table(name = "dataset_info", schema = "public", catalog = "workflow-results")
class DatasetInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int = 0

    @Basic
    @Column(name = "dataset_name")
    var datasetName: String? = null

    @Basic
    @Column(name = "features")
    var features: Int = 0

    @Basic
    @Column(name = "instances")
    var instances: Int = 0

    @Basic
    @Column(name = "classes")
    var classes: Int = 0

    constructor(datasetName: String, features: Int, instances: Int, classes: Int) {
        this.datasetName = datasetName
        this.features = features
        this.instances = instances
        this.classes = classes
    }

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as DatasetInfo

        if (id != other.id) return false
        if (datasetName != other.datasetName) return false
        if (features != other.features) return false
        if (instances != other.instances) return false
        if (classes != other.classes) return false

        return true
    }

    override fun hashCode(): Int{
        var result = id
        result = 31 * result + (datasetName?.hashCode() ?: 0)
        result = 31 * result + features
        result = 31 * result + instances
        result = 31 * result + classes
        return result
    }
}
