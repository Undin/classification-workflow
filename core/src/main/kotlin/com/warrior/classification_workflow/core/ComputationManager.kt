package com.warrior.classification_workflow.core

/**
 * Created by warrior on 26/04/16.
 */
interface ComputationManager {
    fun compute(tasks: List<Workflow>, dataset: String): List<Result>
}
