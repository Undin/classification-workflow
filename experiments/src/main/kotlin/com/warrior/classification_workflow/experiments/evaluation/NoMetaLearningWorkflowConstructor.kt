package com.warrior.classification_workflow.experiments.evaluation

import com.warrior.classification_workflow.Config
import com.warrior.classification_workflow.WorkflowConstructor
import com.warrior.classification_workflow.meta.RandomSelector
import com.warrior.classification_workflow.meta.Selector

class NoMetaLearningWorkflowConstructor(config: Config) : WorkflowConstructor(config) {
    override fun selector(): Selector = RandomSelector()
}
