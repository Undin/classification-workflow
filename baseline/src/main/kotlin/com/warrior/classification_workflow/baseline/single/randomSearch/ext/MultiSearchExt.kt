package com.warrior.classification_workflow.baseline.single.randomSearch.ext

import com.sun.management.OperatingSystemMXBean
import com.warrior.classification_workflow.baseline.single.randomSearch.Result
import weka.classifiers.meta.MultiSearch
import weka.classifiers.meta.multisearch.*
import java.lang.management.ManagementFactory
import java.util.*

class MultiSearchExt : MultiSearch() {

    override fun newFactory(): AbstractEvaluationFactory<*, out AbstractEvaluationWrapper<*, *>, *, *>
            = DefaultEvaluationFactoryExt(startCPUTime, maxCPUTime, output)

    override fun defaultAlgorithm(): AbstractSearch = RandomSearch().apply {
        numIterations = 10000
        numExecutionSlots = 4
        searchSpaceNumFolds = 10
        randomSeed = Random().nextInt()
    }

    // TODO: fix this nightmare
    companion object {
        private val osMXBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        private var startCPUTime: Long = 0
        private var maxCPUTime: Long = 0
        private lateinit var output: MutableList<Result>

        fun init(output: MutableList<Result>, maxCPUTime: Long) {
            this.output = output
            startCPUTime = osMXBean.processCpuTime
            this.maxCPUTime = maxCPUTime
        }
    }
}