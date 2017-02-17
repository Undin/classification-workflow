package com.warrior.classification_workflow.baseline.single

import com.warrior.classification_workflow.baseline.params.ParamsEvaluator
import com.warrior.classification_workflow.core.Classifier
import com.warrior.classification_workflow.core.parallelCrossValidation
import com.warrior.classification_workflow.core.withLog
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import weka.core.Instances
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool

/**
 * Created by warrior on 2/1/17.
 */
class TwoStepGridSearch(
        private val baseClassifier: Classifier,
        private val data: Instances,
        private val folds: Int,
        private val pool: ForkJoinPool,
        private val random: Random
) {

    private val logger: Logger = LogManager.getLogger(javaClass)

    private val classifierName = baseClassifier.name
    private val dataName = data.relationName()
    private val numInstances = data.numInstances()
    private val numAttributes = data.numAttributes()

    private val paramsEvaluator: ParamsEvaluator = ParamsEvaluator()

    fun search(params: List<Param>): Pair<Map<String, Double>, Double> {
        logger.info("start grid search for $classifierName on $dataName")

        // loose step
        val looseResult = logger.withLog("loose step for $classifierName on $dataName") {

            val grids = params.map { p ->
                val looseStart = paramsEvaluator.calculateBound(p.looseStart, numInstances, numAttributes)
                val looseEnd = paramsEvaluator.calculateBound(p.looseEnd, numInstances, numAttributes)
                val looseStep = paramsEvaluator.calculateStep(p.looseStep, numInstances, numAttributes, looseStart, looseEnd)
                Grid(p.name, looseStart, looseEnd, looseStep, p.type)
            }
            val allGridPoints = allGridPoints(grids)
            findBest(allGridPoints)
        }
        logger.info("loose step result for $classifierName on $dataName: $looseResult")

        // fine step
        val fineResult = logger.withLog("fine step for $classifierName on $dataName") {
            val grids = params.map { p ->
                val looseValue = looseResult.first[p.name] ?: throw IllegalStateException("unreachable state")
                val looseStart = paramsEvaluator.calculateBound(p.looseStart, numInstances, numAttributes)
                val looseEnd = paramsEvaluator.calculateBound(p.looseEnd, numInstances, numAttributes)
                val looseStep = paramsEvaluator.calculateStep(p.looseStep, numInstances, numAttributes, looseStart, looseEnd)
                val (fineStart, fineEnd) = fineBounds(looseStart, looseEnd, looseStep, looseValue)
                val fineStep = paramsEvaluator.calculateStep(p.fineStep, numInstances, numAttributes, fineStart, fineEnd)
                Grid(p.name, fineStart, fineEnd, fineStep, p.type)
            }
            val findParamVariants = allGridPoints(grids)
            findBest(findParamVariants)
        }
        logger.info("fine step result for $classifierName on $dataName: $fineResult")
        logger.info("end grid search for $classifierName on $dataName")
        return fineResult
    }

    private fun fineBounds(start: Double, end: Double, step: Double, looseResult: Double): Pair<Double, Double> {
        return Pair(
                Math.max(start, looseResult - step),
                Math.min(end, looseResult + step)
        )
    }

    private fun findBest(paramsList: List<Map<String, Pair<Double, Double>>>): Pair<Map<String, Double>, Double> {
        val tasks = paramsList.map { params ->
            Callable {
                val (name, className, options) = baseClassifier
                val fullOptions = params.mapValuesTo(HashMap()) {
                    // try to use Int for integer params
                    val intValue = it.value.second.toInt()
                    if (intValue.toDouble() == it.value.second) {
                        intValue.toString()
                    } else {
                        it.value.second.toString()
                    }
                }
                fullOptions += options
                val classifier = Classifier(name, className, fullOptions)
                val evaluation = parallelCrossValidation(classifier, data, folds, random)
                val fscore = evaluation.unweightedMacroFmeasure()
                logger.info("$classifierName on $dataName. $fullOptions: $fscore")
                params.mapValues { it.value.first } to fscore
            }
        }

        val futures = pool.invokeAll(tasks)
        return futures.map { it.get() }.maxBy { it.second }!!
    }

    private fun allGridPoints(grids: List<Grid>): List<Map<String, Pair<Double, Double>>> {

        fun generate(k: Int, params: MutableMap<String, Pair<Double, Double>>, out: MutableList<MutableMap<String, Pair<Double, Double>>>) {
            if (k == grids.size) {
                out += HashMap(params)
            } else {
                val (name, start, end, step, paramType) = grids[k]
                for (value in DoubleProgression(start, end, step)) {
                    params[name] = Pair(value, paramType.transform(value))
                    generate(k + 1, params, out)
                }
                params.remove(name)
            }
        }

        val out = ArrayList<MutableMap<String, Pair<Double, Double>>>()
        val params = HashMap<String, Pair<Double, Double>>()
        generate(0, params, out)
        return out
    }

    private data class Grid(
            val name: String,
            val start: Double,
            val end: Double,
            val step: Double,
            val paramType: ParamType
    )
}
