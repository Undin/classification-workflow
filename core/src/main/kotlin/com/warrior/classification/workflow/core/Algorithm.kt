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
sealed class Algorithm : Serializable {
    class Classifier @JsonCreator constructor(
            @JsonProperty("classifier_class") val classifierClass: String,
            @JsonProperty("classifier_options") val options: List<String>
    ) : Algorithm() {
        operator fun invoke(): weka.classifiers.Classifier = AbstractClassifier.forName(classifierClass, options.toTypedArray())
        override fun toString(): String{
            return "Classifier(classifierClass='$classifierClass', options=$options)"
        }
    }
    class Transformer @JsonCreator constructor(
            @JsonProperty("search_class") val searchClass: String,
            @JsonProperty("search_options") val searchOptions: List<String>,
            @JsonProperty("evaluation_class") val evaluationClass: String,
            @JsonProperty("evaluation_options") val evaluationOptions: List<String>
    ) : Algorithm() {
        operator fun invoke(): Pair<ASSearch, ASEvaluation> = Pair(
                ASSearch.forName(searchClass, searchOptions.toTypedArray()),
                ASEvaluation.forName(evaluationClass, evaluationOptions.toTypedArray())
        )
        override fun toString(): String{
            return "Transformer(searchClass='$searchClass', searchOptions=$searchOptions, evaluationClass='$evaluationClass', evaluationOptions=$evaluationOptions)"
        }
    }
}