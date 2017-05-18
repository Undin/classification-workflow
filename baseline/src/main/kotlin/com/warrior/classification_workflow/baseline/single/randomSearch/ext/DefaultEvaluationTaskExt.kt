package com.warrior.classification_workflow.baseline.single.randomSearch.ext

import com.sun.management.OperatingSystemMXBean
import com.warrior.classification_workflow.baseline.single.randomSearch.Result
import weka.classifiers.meta.MultiSearch
import weka.classifiers.meta.multisearch.DefaultEvaluationTask
import weka.core.Instances
import weka.core.SetupGenerator
import weka.core.setupgenerator.Point
import java.lang.management.ManagementFactory

class DefaultEvaluationTaskExt(
        private val startCPUTime: Long,
        private val maxCPUTime: Long,
        private val output: MutableList<Result>,
        owner: MultiSearch, train: Instances, test: Instances?, generator: SetupGenerator, val values: Point<Any>, folds: Int, eval: Int, classLabel: Int)
    : DefaultEvaluationTask(owner, train, test, generator, values, folds, eval, classLabel) {

    override fun call(): Boolean {
        return if (osMXBean.processCpuTime - startCPUTime > maxCPUTime) {
            true
        } else {
            super.call().also {
                val cpuTime = osMXBean.processCpuTime - startCPUTime
                println(cpuTime)
                if (cpuTime > maxCPUTime) {
                    println("time is over!!!")
                } else {
                    synchronized(output) {
                        output += Result(cpuTime, values)
                    }
                }
            }
        }
    }

    companion object {
        private val osMXBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    }
}
