package com.warrior.classification_workflow.stacking

import com.warrior.classification_workflow.core.Workflow
import com.warrior.classification_workflow.core.WorkflowClassifier
import weka.classifiers.meta.Stacking
import weka.core.*

class WorkflowStacking : Stacking() {

    private lateinit var workflows: List<Workflow>
    private lateinit var workflowClassifiers: List<WorkflowClassifier>

    fun setWorkflows(workflows: List<Workflow>) {
        this.workflows = workflows
        workflowClassifiers = workflows.map { it.classifier() }
        classifiers = workflowClassifiers.toTypedArray()
    }

    override fun metaInstance(instance: Instance): Instance {
        val values = DoubleArray(m_MetaFormat!!.numAttributes())
        var index = 0
        for (classifier in workflowClassifiers) {
            val distributions = classifier.classify(instance)
            for (distribution in distributions) {
                for (probability in distribution) {
                    values[index++] = probability
                }
            }
        }
        values[index] = instance.classValue()
        val metaInstance = DenseInstance(1.0, values)
        metaInstance.setDataset(m_MetaFormat)
        return metaInstance
    }

    override fun metaFormat(instances: Instances): Instances {
        val attributes = ArrayList<Attribute>()
        for (workflow in workflows) {
            for (algo in workflow.allAlgorithms) {
                if (algo is com.warrior.classification_workflow.core.Classifier) {
                    for (i in 0 until instances.numClasses()) {
                        attributes += Attribute("${algo.name}-${attributes.size}")
                    }
                }
            }
        }
        attributes += instances.classAttribute().copy() as Attribute
        val metaFormat = Instances("Meta format", attributes, 0)
        metaFormat.setClassIndex(metaFormat.numAttributes() - 1)
        return metaFormat
    }
}
