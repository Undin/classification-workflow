package com.warrior.classification_workflow.meta_learning.calculators

import com.warrior.classification_workflow.meta_learning.Classifier
import com.warrior.classification_workflow.meta_learning.toArray
import weka.classifiers.AbstractClassifier
import weka.classifiers.AggregateableEvaluation
import weka.classifiers.Evaluation
import weka.core.Instances
import java.util.*
import java.util.stream.IntStream
import java.util.stream.Stream

/**
 * Created by warrior on 11/28/16.
 */
abstract class AbstractPerformanceEvaluator : Evaluator {

    private val crossValidationIterations = 10
    private val crossValidationFolders = 10

    protected fun crossValidation(classifier: Classifier, data: Instances, random: Random): Double {
        val options = classifier.options.toArray()
        val wekaClassifier = AbstractClassifier.forName(classifier.className, options)

        val fullAggregation: AggregateableEvaluation = IntStream.range(0, crossValidationIterations)
                .parallel()
                .boxed()
                .flatMap { parallelCrossValidation(data, wekaClassifier, random, crossValidationFolders) }
                .collect(
                        { AggregateableEvaluation(data) },
                        { acc, o -> acc.aggregate(o) },
                        { l, r -> l.aggregate(r) }
                )
        return fullAggregation.unweightedMacroFmeasure()
    }

    protected fun parallelCrossValidation(data: Instances, classifier: weka.classifiers.Classifier,
                                          random: Random, numFolds: Int): Stream<Evaluation> {
        val shuffledData = Instances(data)
        shuffledData.randomize(random)
        return IntStream.range(0, numFolds)
                .parallel()
                .mapToObj { fold ->
                    val train = shuffledData.trainCV(numFolds, fold, random)
                    val copiedClassifier = AbstractClassifier.makeCopy(classifier)
                    copiedClassifier.buildClassifier(train)
                    val test = shuffledData.testCV(numFolds, fold)
                    val eval = Evaluation(shuffledData)
                    eval.evaluateModel(copiedClassifier, test)
                    eval
                }
    }
}
