package com.warrior.classification_workflow.core.meta.features

import com.warrior.classification_workflow.core.meta.features.general.*
import com.warrior.classification_workflow.core.meta.features.informationtheoretic.MeanMutualInformation
import com.warrior.classification_workflow.core.meta.features.statistical.MeanStandardDeviation
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsNull.notNullValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import weka.attributeSelection.InfoGainAttributeEval
import weka.core.Attribute
import weka.core.Instances
import weka.core.converters.ConverterUtils
import java.util.*

/**
 * Created by warrior on 11/16/16.
 */
class Tests {

    private val DELTA = 0.001
    private val random: Random = Random()
    private lateinit var instances: Instances

    @Before
    fun before() {
        instances = loadFromResources("dataset.arff")
    }

    @Test
    fun forNameTest() {
        val metaFeature = AbstractMetaFeatureExtractor.forName(NumberOfInstances::class.java.name)
        assertThat(metaFeature, notNullValue())
    }

    @Test
    fun generalFeaturesTest() {
        val metaFeatures = listOf(
                NumberOfInstances(),
                NumberOfFeatures(),
                NumberOfClasses(),
                DataSetDimensionality()
        )
        val values = listOf(
                instances.numInstances().toDouble(),
                instances.numAttributes().toDouble() - 1,
                instances.numClasses().toDouble(),
                instances.numInstances().toDouble() / (instances.numAttributes() - 1)
        )
        metaFeatures.zip(values)
                .forEach {
                    val (feature, value) = it
                    feature.instances = instances
                    assertThat(feature.compute(), equalTo(value))
                }
    }

    @Test
    fun attributeFeaturesTest() {
        val std = MeanStandardDeviation()
        std.instances = instances
        val value = std.compute()
        val expectedValue = meanStandardDeviation(instances)
        assertEquals(expectedValue, value, DELTA)
    }

    @Test
    fun changeInstanceTest() {
        val std = MeanStandardDeviation()
        std.instances = instances
        std.compute()

        val attr = Attribute("new-attribute")
        instances.insertAttributeAt(attr, 0)
        for (instance in instances) {
            instance.setValue(0, random.nextDouble())
        }

        val value = std.compute()
        val expectedValue = meanStandardDeviation(instances)
        assertEquals(expectedValue, value, DELTA)
    }

    @Test
    fun mutualInformationTest() {
        val mu = MeanMutualInformation()
        mu.instances = instances
        var value = mu.compute()
        var expectedValue = meanMutualInformation(instances)
        assertEquals(expectedValue, value, DELTA)

        val attr = Attribute("new-attribute")
        instances.insertAttributeAt(attr, 0)
        for (instance in instances) {
            instance.setValue(0, random.nextDouble())
        }

        value = mu.compute()
        expectedValue = meanMutualInformation(instances)
        assertEquals(expectedValue, value, DELTA)
    }

    private fun meanStandardDeviation(instances: Instances): Double {
        return (0 until instances.numAttributes())
                .filter { it != instances.classIndex() }
                .map { StandardDeviation().evaluate(instances.attributeToDoubleArray(it)) }
                .average()
    }

    private fun meanMutualInformation(instances: Instances): Double {
        val infoGain = InfoGainAttributeEval()
        infoGain.buildEvaluator(instances)
        return (0 until instances.numAttributes())
                .filter { it != instances.classIndex() }
                .map { infoGain.evaluateAttribute(it) }
                .average()
    }

    private fun loadFromResources(path: String): Instances {
        val datasetStream = Tests::class.java.classLoader.getResourceAsStream(path)
        val instances = ConverterUtils.DataSource.read(datasetStream)
        instances.setClassIndex(instances.numAttributes() - 1)
        return instances
    }
}
