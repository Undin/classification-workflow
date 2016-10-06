package com.warrior.classification.workflow

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.warrior.classification.workflow.core.load
import com.warrior.classification.workflow.core.save
import weka.attributeSelection.PrincipalComponents

/**
 * Created by warrior on 14/09/16.
 */
fun main(args: Array<String>) {
//    val dataset = load("datasets/zoo.arff")
//    val pca = PrincipalComponents()
//    pca.buildEvaluator(dataset)
//    val newDataset = pca.transformedData(dataset)
//    for (i in 0 until newDataset.numAttributes()) {
//        val attr = newDataset.attribute(i)
//        if (attr.isNumeric) {
//            println("${attr.name()}: (${attr.lowerNumericBound}; ${attr.upperNumericBound})")
//        }
//    }
//    val mapper = ObjectMapper()
//    println(mapper.writeValueAsString(A(1)))
    val instances = load("datasets-tmp/winequality-white.csv")
    save(instances, "datasets-tmp/winequality-white.arff")
}

class A(@get:JsonProperty("field_a") val fieldA: Int)