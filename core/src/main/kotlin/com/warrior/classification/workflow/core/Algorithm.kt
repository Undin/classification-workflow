package com.warrior.classification.workflow.core

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import weka.attributeSelection.ASEvaluation
import weka.attributeSelection.ASSearch
import weka.classifiers.AbstractClassifier
import java.io.Serializable

/**
 * Created by warrior on 27/08/16.
 */
sealed class Algorithm(val name: String) : Serializable {
    class Classifier @JsonCreator constructor(
            @JsonProperty("name") name: String,
            @JsonProperty("classifier_class") val classifierClass: String,
            @JsonProperty("classifier_options") val options: List<String>
    ) : Algorithm(name) {
        operator fun invoke(): weka.classifiers.Classifier = AbstractClassifier.forName(classifierClass, options.toTypedArray())
    }
    class Transformer @JsonCreator constructor(
            @JsonProperty("name") name: String,
            @JsonProperty("search_class") val searchClass: String,
            @JsonProperty("search_options") val searchOptions: List<String>,
            @JsonProperty("evaluation_class") val evaluationClass: String,
            @JsonProperty("evaluation_options") val evaluationOptions: List<String>
    ) : Algorithm(name) {
        operator fun invoke(): Pair<ASSearch, ASEvaluation> = Pair(
                ASSearch.forName(searchClass, searchOptions.toTypedArray()),
                ASEvaluation.forName(evaluationClass, evaluationOptions.toTypedArray())
        )
    }

    override fun toString(): String = name
}