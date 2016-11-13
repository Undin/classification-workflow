package com.warrior.classification_workflow.core

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import weka.attributeSelection.ASEvaluation
import weka.attributeSelection.ASSearch
import weka.classifiers.AbstractClassifier
import weka.classifiers.Classifier
import java.io.Serializable

/**
 * Created by warrior on 27/08/16.
 */
sealed class AlgorithmConfiguration(@get:JsonProperty("name") val name: String) : Serializable {
    class ClassifierConfiguration @JsonCreator constructor(
            @JsonProperty("name") name: String,
            @get:JsonProperty("classifier_class") @param:JsonProperty("classifier_class") val classifierClass: String,
            @get:JsonProperty("classifier_options") @param:JsonProperty("classifier_options") val classifierOptions: Map<String, List<String>>
    ) : AlgorithmConfiguration(name)

    class TransformerConfiguration @JsonCreator constructor(
            @JsonProperty("name") name: String,
            @get:JsonProperty("search_class") @param:JsonProperty("search_class") val searchClass: String,
            @get:JsonProperty("search_options") @param:JsonProperty("search_options") val searchOptions: Map<String, List<String>>,
            @get:JsonProperty("evaluation_class") @param:JsonProperty("evaluation_class") val evaluationClass: String,
            @get:JsonProperty("evaluation_options") @param:JsonProperty("evaluation_options") val evaluationOptions: Map<String, List<String>>
    ) : AlgorithmConfiguration(name)
}

sealed class Algorithm() : Serializable {
    class Classifier @JsonCreator constructor(
            @get:JsonProperty("name") @param:JsonProperty("name") val name: String,
            @get:JsonProperty("classifier_class") @param:JsonProperty("classifier_class") val classifierClass: String,
            @get:JsonProperty("classifier_options") @param:JsonProperty("classifier_options") val options: Map<String, String>
    ) : Algorithm() {
        operator fun invoke(): weka.classifiers.Classifier = AbstractClassifier.forName(classifierClass, options.toStringArray())
        override fun toString(): String = "$name$options"
    }

    class Transformer @JsonCreator constructor(
            @get:JsonProperty("name") @param:JsonProperty("name") val name: String,
            @get:JsonProperty("search_class") @param:JsonProperty("search_class") val searchClass: String,
            @get:JsonProperty("search_options") @param:JsonProperty("search_options") val searchOptions: Map<String, String>,
            @get:JsonProperty("evaluation_class") @param:JsonProperty("evaluation_class") val evaluationClass: String,
            @get:JsonProperty("evaluation_options") @param:JsonProperty("evaluation_options") val evaluationOptions: Map<String, String>
    ) : Algorithm() {

        operator fun invoke(): Pair<ASSearch, ASEvaluation> = Pair(
                ASSearch.forName(searchClass, searchOptions.toStringArray()),
                ASEvaluation.forName(evaluationClass, evaluationOptions.toStringArray())
        )

        override fun toString(): String {
            return "$name{" +
                    "searchOptions=$searchOptions, " +
                    "evaluationOptions=$evaluationOptions}"
        }
    }

    protected fun Map<String, String>.toStringArray() = flatMap { listOf(it.key, it.value) }.toTypedArray()
}

