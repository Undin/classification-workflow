package com.warrior.classification_workflow.meta.evaluation.evaluators

import com.warrior.classification_workflow.core.Classifier
import com.warrior.classification_workflow.core.storage.SaveStrategy
import com.warrior.classification_workflow.meta.evaluation.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import weka.classifiers.AbstractClassifier
import weka.classifiers.AggregateableEvaluation
import weka.classifiers.Evaluation
import weka.core.Instances
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.stream.IntStream

/**
 * Created by warrior on 11/28/16.
 */
abstract class AbstractPerformanceEvaluator(private val pool: ForkJoinPool) : Evaluator {

    private val crossValidationFolders = 10

    protected val logger: Logger = LogManager.getLogger(javaClass)

    protected abstract val saveStrategy: SaveStrategy

    override fun evaluate() {
        val tasks = getTasks()
        try {
            for (task in tasks) {
                pool.submit(task)
            }
            for (task in tasks) {
                task.get()
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
        } finally {
            saveStrategy.close()
        }
    }

    protected fun crossValidation(classifier: Classifier, data: Instances, random: Random): Double {
        val options = classifier.options.toArray()
        val wekaClassifier = AbstractClassifier.forName(classifier.className, options)

        val shuffledData = Instances(data)
        shuffledData.randomize(random)

        val fullAggregation: AggregateableEvaluation = IntStream.range(0, crossValidationFolders)
                .parallel()
                .mapToObj { fold ->
                    logger.withLog("${classifier.name} on ${data.relationName()}: fold $fold") {
                        val train = shuffledData.trainCV(crossValidationFolders, fold, random)
                        val copiedClassifier = AbstractClassifier.makeCopy(wekaClassifier)
                        copiedClassifier.buildClassifier(train)
                        val test = shuffledData.testCV(crossValidationFolders, fold)
                        val eval = Evaluation(shuffledData)
                        eval.evaluateModel(copiedClassifier, test)
                        eval
                    }
                }.collect(
                    { AggregateableEvaluation(data) },
                    { acc, o -> acc.aggregate(o) },
                    { l, r -> l.aggregate(r) }
                )
        return fullAggregation.unweightedMacroFmeasure()
    }

    protected abstract fun getTasks(): List<ForkJoinTask<*>>
}
