package com.warrior.classification_workflow.baseline.single.xgboost

import ml.dmlc.xgboost4j.java.Booster
import ml.dmlc.xgboost4j.java.DMatrix
import ml.dmlc.xgboost4j.java.XGBoost
import weka.classifiers.AbstractClassifier
import weka.core.Instance
import weka.core.Instances
import weka.core.Utils

class XGBClassifier : AbstractClassifier() {

    private lateinit var booster: Booster

    var threads: Int = 1
    var maxDepth: Int = 6
    var minChildWeight: Int = 1
    var gamma: Float = 0.0F
    var rounds = 500

    override fun buildClassifier(data: Instances) {
        val matrix = data.toDMatrix()
        val params = mapOf(
                "objective" to "multi:softmax",
                "silent" to 1,
                "num_class" to data.numClasses(),
                "nthread" to threads,
                "max_depth" to maxDepth,
                "min_child_weight" to minChildWeight,
                "gamma" to gamma)
        booster = XGBoost.train(matrix, params, rounds, emptyMap(), null, null)
    }

    override fun classifyInstance(instance: Instance): Double {
        val instances = Instances(instance.dataset(), 0)
        instances += instance.copy() as Instance
        val predict = booster.predict(instances.toDMatrix())
        return predict[0][0].toDouble()
    }

    override fun setOptions(options: Array<out String>) {
        super.setOptions(options)
        var option = Utils.getOption('D', options)
        if (option.isNotEmpty()) {
            maxDepth = option.toInt()
        }
        option = Utils.getOption('W', options)
        if (option.isNotEmpty()) {
            minChildWeight = option.toInt()
        }
        option = Utils.getOption('G', options)
        if (option.isNotEmpty()) {
            gamma = option.toFloat()
        }
        option = Utils.getOption('R', options)
        if (option.isNotEmpty()) {
            rounds = option.toInt()
        }
    }

    private fun Instances.toDMatrix(): DMatrix {
        val values = FloatArray((numAttributes() - 1) * numInstances())
        var index = 0

        for (instance in this) {
            for (i in 0 until numAttributes()) {
                if (i != classIndex()) {
                    values[index] = instance.value(i).toFloat()
                    index++
                }
            }
        }
        val matrix = DMatrix(values, numInstances(), numAttributes() - 1)
        val labels = FloatArray(numInstances()) { i -> get(i).classValue().toFloat() }
        matrix.label = labels
        return matrix
    }
}
