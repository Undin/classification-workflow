package com.warrior.classification_workflow.core

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Created by warrior on 12/11/16.
 */
class SerializationTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun classifierTest() {
        val algorithm = Classifier(
                name = "C4.5",
                className = "weka.classifiers.trees.J48",
                options = mapOf("-D" to "2")
        )
        val serializedAlgorithm = mapper.writeValueAsString(algorithm)
        val deserializedAlgorithm: Classifier = mapper.readValue(serializedAlgorithm)
        assertThat(deserializedAlgorithm, equalTo(algorithm))
    }

    @Test
    fun transformerTest() {
        val algorithm = Transformer(
                name = "Signific",
                search = Search(
                        className = "weka.attributeSelection.Ranker",
                        options = mapOf("-T" to "0.01")
                ),
                evaluation = Evaluation(
                        className = "weka.attributeSelection.SignificanceAttributeEval",
                        options = emptyMap()
                )
        )
        val serializedAlgorithm = mapper.writeValueAsString(algorithm)
        val deserializedAlgorithm: Transformer = mapper.readValue(serializedAlgorithm)
        assertThat(deserializedAlgorithm, equalTo(algorithm))
    }

    @Test
    fun classifierConfigurationTest() {
        val configuration = ClassifierConfiguration(
                name = "C4.5",
                classifierClass = "weka.classifiers.trees.J48",
                classifierOptions = mapOf("-D" to listOf("1", "2", "5"))
        )
        val serializedConfiguration = mapper.writeValueAsString(configuration)
        val deserializedConfiguration: ClassifierConfiguration = mapper.readValue(serializedConfiguration)
        assertThat(deserializedConfiguration, equalTo(configuration))
    }

    @Test
    fun transformerConfigurationTest() {
        val configuration = TransformerConfiguration(
                name = "CFS-SFS",
                searchConfiguration = SearchConfiguration(
                        className = "weka.attributeSelection.BestFirst",
                        options = mapOf("-D" to listOf("0", "1", "2"))
                ),
                evaluationConfiguration = EvaluationConfiguration(
                        className = "weka.attributeSelection.CfsSubsetEval",
                        options = emptyMap()
                )
        )
        val serializedConfiguration = mapper.writeValueAsString(configuration)
        val deserializedConfiguration: TransformerConfiguration = mapper.readValue(serializedConfiguration)
        assertThat(deserializedConfiguration, equalTo(configuration))
    }

    @Test
    fun workflowTest() {
        val transformer = Transformer(
                name = "Signific",
                search = Search(
                        className = "weka.attributeSelection.Ranker",
                        options = mapOf("-T" to "0.01")
                ),
                evaluation = Evaluation(
                        className = "weka.attributeSelection.SignificanceAttributeEval",
                        options = emptyMap()
                )
        )
        val classifier = Classifier(
                name = "C4.5",
                className = "weka.classifiers.trees.J48",
                options = mapOf("-D" to "2")
        )
        val workflow = Workflow(listOf(transformer), classifier)
        val serializedWorkflow = mapper.writeValueAsString(workflow)
        val deserializedWorkflow: Workflow = mapper.readValue(serializedWorkflow)
        assertThat(deserializedWorkflow, equalTo(workflow))
    }
}
