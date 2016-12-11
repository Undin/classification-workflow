package com.warrior.classification_workflow.core

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import weka.classifiers.Classifier
import java.util.*

/**
 * Created by warrior on 12/07/16.
 */
class Workflow @JsonCreator constructor(
        @JsonProperty("algorithms") val algorithms: List<Algorithm>,
        @JsonProperty("classifier") val classifier: Algorithm.Classifier
) {

    @JsonIgnore
    val allAlgorithms = algorithms + classifier

    constructor(algorithms: List<Algorithm>) : this(
            ArrayList(algorithms.subList(0, algorithms.lastIndex)),
            algorithms.last() as Algorithm.Classifier
    )

    fun classifier(): Classifier = WorkflowClassifier(algorithms, classifier)

    override fun toString(): String {
        return "Workflow(" +
                "algorithms=$algorithms," +
                "classifier=$classifier" +
                ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Workflow

        if (algorithms != other.algorithms) return false
        if (classifier != other.classifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = algorithms.hashCode()
        result = 31 * result + classifier.hashCode()
        return result
    }
}
