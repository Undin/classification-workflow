package com.warrior.classification_workflow.experiments.plot

import com.github.benmanes.caffeine.cache.Cache
import com.sun.management.OperatingSystemMXBean
import com.warrior.classification_workflow.LocalComputationManager
import com.warrior.classification_workflow.core.ClassifierConfiguration
import com.warrior.classification_workflow.core.TransformerConfiguration
import com.warrior.classification_workflow.core.Workflow
import com.warrior.classification_workflow.meta.AlgorithmChooser
import weka.core.Instances
import java.lang.management.ManagementFactory

class CPUTimeComputationManager(
        private val output: MutableList<Result>,
        instances: Instances,
        algorithmChooser: AlgorithmChooser,
        classifiersMap: Map<String, ClassifierConfiguration>,
        transformersMap: Map<String, TransformerConfiguration>,
        cache: Cache<String, MutableMap<Int, Instances>>,
        cachePrefixSize: Int, threads: Int
) : LocalComputationManager(instances, algorithmChooser, classifiersMap,
        transformersMap, cache, cachePrefixSize, threads) {

    private val osMXBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    private val startCPUTime = osMXBean.processCpuTime

    override fun compute(workflow: Workflow, train: Instances, test: Instances): Double {
        return super.compute(workflow, train, test).also {
            synchronized(output) {
                val cpuTime = osMXBean.processCpuTime - startCPUTime
                println(cpuTime)
                output += Result(workflow, cpuTime)
            }
        }
    }
}
