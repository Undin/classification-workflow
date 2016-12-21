package com.warrior.classification_workflow.core

import com.fasterxml.jackson.annotation.*
import weka.attributeSelection.ASEvaluation
import weka.attributeSelection.ASSearch
import weka.classifiers.AbstractClassifier
import java.io.Serializable

/**
 * Created by warrior on 27/08/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes(
        JsonSubTypes.Type(value = Classifier::class, name = "Classifier"),
        JsonSubTypes.Type(value = Transformer::class, name = "Transformer")
)
interface Algorithm : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class Classifier(
        @JsonProperty("name") val name: String,
        @JsonProperty("classifier_class") val className: String,
        @JsonProperty("classifier_options") val options: Map<String, String>
) : Algorithm {
    operator fun invoke(): weka.classifiers.Classifier = AbstractClassifier.forName(className, options.toStringArray())
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Transformer(
        @JsonProperty("name") val name: String,
        @JsonProperty("search") val search: Search,
        @JsonProperty("evaluation") val evaluation: Evaluation
) : Algorithm {
    operator fun invoke(): Pair<ASSearch, ASEvaluation> = Pair(search(), evaluation())
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Search(
        @JsonProperty("search_class") val className: String,
        @JsonProperty("search_options") val options: Map<String, String>
) : Serializable {
    operator fun invoke(): ASSearch = ASSearch.forName(className, options.toStringArray())
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Evaluation(
        @JsonProperty("evaluation_class") val className: String,
        @JsonProperty("evaluation_options") val options: Map<String, String>
) : Serializable {
    operator fun invoke(): ASEvaluation = ASEvaluation.forName(className, options.toStringArray())
}

private fun Map<String, String>.toStringArray() = flatMap { listOf(it.key, it.value) }.toTypedArray()
