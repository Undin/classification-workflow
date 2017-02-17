package com.warrior.classification_workflow.baseline.params

import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsEqual.*
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Created by warrior on 2/17/17.
 */
class ParamsEvaluatorTest {

    private val evaluator = ParamsEvaluator()

    @Test
    fun constTest() {
        val result = evaluator.calculateBound("5", 10, 10)
        assertThat(result, equalTo(5.0))
    }

    @Test
    fun attributesVarTest() {
        val attributes = 5
        val result = evaluator.calculateBound("attributes", 10, attributes)
        assertThat(result, equalTo(attributes.toDouble()))
    }

    @Test
    fun instancesVarTest() {
        val instances = 5
        val result = evaluator.calculateBound("instances", instances, 10)
        assertThat(result, equalTo(instances.toDouble()))
    }

    @Test
    fun arithmeticTest() {
        val attributes = 23
        val result = evaluator.calculateBound("attributes / 5", 10, attributes)
        assertThat(result, equalTo(attributes.toDouble() / 5))
    }

    @Test
    fun sqrtTest() {
        val attributes = 23
        val result = evaluator.calculateBound("sqrt(attributes)", 10, attributes)
        assertThat(result, equalTo(Math.sqrt(attributes.toDouble())))
    }

    @Test(expected = Exception::class)
    fun boundVarsInCalculateBoundTest() {
        evaluator.calculateBound("attributes - start", 10, 10)
    }

    @Test
    fun boundVarsTest() {
        val start = 1.0
        val end = 10.0
        val result = evaluator.calculateStep("end - start", 10, 10, start, end)
        assertThat(result, equalTo(end - start))
    }
}
