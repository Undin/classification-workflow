package com.warrior.classification_workflow

import com.warrior.classification_workflow.core.Workflow

/**
 * Created by warrior on 26/04/16.
 */
interface ComputationManager {
    fun compute(tasks: List<Workflow>, dataset: String): List<Result>
}
