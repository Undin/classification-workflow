package com.warrior.classification_workflow.core

import com.fasterxml.jackson.annotation.*
import com.warrior.classification_workflow.core.meta.features.CommonMetaFeatureExtractor
import java.util.*

/**
 * Created by warrior on 12/07/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Workflow(
        @JsonIgnore val uuid: String,
        @JsonProperty("algorithms") val algorithms: List<Algorithm>,
        @JsonProperty("classifier") val classifier: Classifier
) {
    @JsonIgnore
    val allAlgorithms = algorithms + classifier

    @JsonIgnore
    var extractor: ThreadLocal<CommonMetaFeatureExtractor>? = null

    @JsonCreator
    constructor(@JsonProperty("algorithms") algorithms: List<Algorithm>,
                @JsonProperty("classifier") classifier: Classifier): this(UUID.randomUUID().toString(), algorithms, classifier)

    constructor(uuid: String, algorithms: List<Algorithm>) : this(
            uuid,
            ArrayList(algorithms.subList(0, algorithms.lastIndex)),
            algorithms.last() as Classifier
    )

    constructor(uuid: String, algorithms: List<Algorithm>, classifier: Classifier, extractor: CommonMetaFeatureExtractor):
            this(uuid, algorithms, classifier) {
        this.extractor = ThreadLocal()
        this.extractor?.set(extractor)
    }

    fun classifier(): WorkflowClassifier = WorkflowClassifier(algorithms, classifier)

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
