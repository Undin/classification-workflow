package com.warrior.classification_workflow.core

import weka.core.Instances
import weka.core.converters.ArffSaver
import weka.core.converters.ConverterUtils
import weka.core.converters.Saver
import java.io.File

/**
 * Created by warrior on 12/07/16.
 */
fun load(path: String): Instances {
    val instances = ConverterUtils.DataSource.read(path)
    instances.setClassIndex(instances.numAttributes() - 1)
    return instances
}

fun save(dataSet: Instances, dst: String, saver: Saver = ArffSaver()) {
    saver.setInstances(dataSet)
    saver.setFile(File(dst))
    saver.writeBatch()
}
