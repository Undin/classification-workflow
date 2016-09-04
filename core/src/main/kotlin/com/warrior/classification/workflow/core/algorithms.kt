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
sealed class AlgorithmConfiguration(val name: String) : Serializable {
    class ClassifierConfiguration @JsonCreator constructor(
            @JsonProperty("name") name: String,
            @JsonProperty("classifier_class") val classifierClass: String,
            @JsonProperty("classifier_options") val classifierOptions: Map<String, List<String>>
    ) : AlgorithmConfiguration(name)

    class TransformerConfiguration @JsonCreator constructor(
            @JsonProperty("name") name: String,
            @JsonProperty("search_class") val searchClass: String,
            @JsonProperty("search_options") val searchOptions: Map<String, List<String>>,
            @JsonProperty("evaluation_class") val evaluationClass: String,
            @JsonProperty("evaluation_options") val evaluationOptions: Map<String, List<String>>
    ) : AlgorithmConfiguration(name)
}

sealed class Algorithm() : Serializable {
    class Classifier(val name: String,
                     val classifierClass: String,
                     val options: Map<String, String>) : Algorithm() {

        operator fun invoke(): weka.classifiers.Classifier = AbstractClassifier.forName(classifierClass, options.toStringArray())
        override fun toString(): String = "$name$options"
    }

    class Transformer(val name: String,
                      val searchClass: String,
                      val searchOptions: Map<String, String>,
                      val evaluationClass: String,
                      val evaluationOptions: Map<String, String>) : Algorithm() {

        operator fun invoke(): Pair<ASSearch, ASEvaluation> = Pair(
                ASSearch.forName(searchClass, searchOptions.toStringArray()),
                ASEvaluation.forName(evaluationClass, evaluationOptions.toStringArray())
        )

        override fun toString(): String {
            return "$name{" +
                    "searchOptions=$searchOptions, " +
                    "evaluationOptions=$evaluationOptions"
        }
    }

    protected fun Map<String, String>.toStringArray() = flatMap { listOf(it.key, it.value) }.toTypedArray()
}

