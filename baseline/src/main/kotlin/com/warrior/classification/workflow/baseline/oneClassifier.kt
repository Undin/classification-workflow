package com.warrior.classification.workflow.baseline

import com.fasterxml.jackson.databind.ObjectMapper
import com.warrior.classification.workflow.core.load
import weka.classifiers.Classifier
import weka.classifiers.evaluation.Evaluation
import weka.classifiers.trees.RandomForest
import weka.core.Instances
import weka.gui.ExtensionFileFilter
import java.io.File
import java.util.*

/**
 * Created by warrior on 17/07/16.
 */
fun main(args: Array<String>) {
    val mapper = ObjectMapper()
    val algo = RandomForest()
    val files = File("datasets").listFiles(ExtensionFileFilter("arff", ""))
    val outputDir = File("baseline-results")
    outputDir.mkdir()
    if (files != null) {
        Arrays.stream(files)
                .parallel()
                .forEach { file ->
                    val data = load(file.absolutePath)
                    println("start ${file.nameWithoutExtension}")
                    val result = measure(algo, data, file.nameWithoutExtension)
                    println("end ${file.nameWithoutExtension}")
                    mapper.writeValue(File(outputDir, "${file.nameWithoutExtension}.json"), result)
                }
    }
}

private fun measure(algo: Classifier, data: Instances, name: String): Result {
    val eval = Evaluation(data)
    eval.crossValidateModel(algo, data, 10, Random())
    return Result(name, algo.javaClass.name, eval.unweightedMacroFmeasure())
}

data class Result(
        val name: String,
        val algorithm: String,
        val fMeasure: Double
)
