package com.warrior.classification.workflow.core

import weka.attributeSelection.ASEvaluation
import weka.attributeSelection.ASSearch
import weka.classifiers.AbstractClassifier
import java.io.Serializable

/**
 * Created by warrior on 27/08/16.
 */
sealed class Algorithm : Serializable {
    class Classifier(val name: String, val options: List<String>) : Algorithm() {
        operator fun invoke(): weka.classifiers.Classifier = AbstractClassifier.forName(name, options.toTypedArray())
    }
    class Transformer(val searchName: String, val searchOptions: List<String>,
                      val evaluationName: String, val evaluationOptions: List<String>) : Algorithm() {
        operator fun invoke(): Pair<ASSearch, ASEvaluation> = Pair(
                ASSearch.forName(searchName, searchOptions.toTypedArray()),
                ASEvaluation.forName(evaluationName, evaluationOptions.toTypedArray())
        )
    }
}