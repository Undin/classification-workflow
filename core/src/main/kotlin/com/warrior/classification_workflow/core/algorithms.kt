package com.warrior.classification_workflow.core

import com.fasterxml.jackson.annotation.JsonProperty
import weka.attributeSelection.ASEvaluation
import weka.attributeSelection.ASSearch
import weka.classifiers.AbstractClassifier
import java.io.Serializable

/**
 * Created by warrior on 27/08/16.
 */
sealed class AlgorithmConfiguration(@JsonProperty("name") val name: String) : Serializable {
    class ClassifierConfiguration(
            @JsonProperty("name") name: String,
            @JsonProperty("classifier_class") val classifierClass: String,
            @JsonProperty("classifier_options") val classifierOptions: Map<String, List<String>>
    ) : AlgorithmConfiguration(name) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as ClassifierConfiguration

            if (classifierClass != other.classifierClass) return false
            if (classifierOptions != other.classifierOptions) return false

            return true
        }

        override fun hashCode(): Int {
            var result = classifierClass.hashCode()
            result = 31 * result + classifierOptions.hashCode()
            return result
        }
    }

    class TransformerConfiguration(
            @JsonProperty("name") name: String,
            @JsonProperty("search_class") val searchClass: String,
            @JsonProperty("search_options") val searchOptions: Map<String, List<String>>,
            @JsonProperty("evaluation_class") val evaluationClass: String,
            @JsonProperty("evaluation_options") val evaluationOptions: Map<String, List<String>>
    ) : AlgorithmConfiguration(name) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as TransformerConfiguration

            if (searchClass != other.searchClass) return false
            if (searchOptions != other.searchOptions) return false
            if (evaluationClass != other.evaluationClass) return false
            if (evaluationOptions != other.evaluationOptions) return false

            return true
        }

        override fun hashCode(): Int {
            var result = searchClass.hashCode()
            result = 31 * result + searchOptions.hashCode()
            result = 31 * result + evaluationClass.hashCode()
            result = 31 * result + evaluationOptions.hashCode()
            return result
        }
    }
}

sealed class Algorithm() : Serializable {
    class Classifier(
            @JsonProperty("name") val name: String,
            @JsonProperty("classifier_class") val classifierClass: String,
            @JsonProperty("classifier_options") val classifierOptions: Map<String, String>
    ) : Algorithm() {
        operator fun invoke(): weka.classifiers.Classifier = AbstractClassifier.forName(classifierClass, classifierOptions.toStringArray())
        override fun toString(): String = "$name$classifierOptions"
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Classifier

            if (name != other.name) return false
            if (classifierClass != other.classifierClass) return false
            if (classifierOptions != other.classifierOptions) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + classifierClass.hashCode()
            result = 31 * result + classifierOptions.hashCode()
            return result
        }
    }

    class Transformer(
            @JsonProperty("name") val name: String,
            @JsonProperty("search_class") val searchClass: String,
            @JsonProperty("search_options") val searchOptions: Map<String, String>,
            @JsonProperty("evaluation_class") val evaluationClass: String,
            @JsonProperty("evaluation_options") val evaluationOptions: Map<String, String>
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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Transformer

            if (name != other.name) return false
            if (searchClass != other.searchClass) return false
            if (searchOptions != other.searchOptions) return false
            if (evaluationClass != other.evaluationClass) return false
            if (evaluationOptions != other.evaluationOptions) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + searchClass.hashCode()
            result = 31 * result + searchOptions.hashCode()
            result = 31 * result + evaluationClass.hashCode()
            result = 31 * result + evaluationOptions.hashCode()
            return result
        }
    }

    protected fun Map<String, String>.toStringArray() = flatMap { listOf(it.key, it.value) }.toTypedArray()
}
