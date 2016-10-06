package com.warrior.classification.workflow

import com.warrior.classification.workflow.core.ComputationManager
import com.warrior.classification.workflow.core.Result
import com.warrior.classification.workflow.core.Workflow

/**
 * Created by warrior on 27/04/16.
 */
class RemoteComputationManager : ComputationManager {

    override fun compute(tasks: List<Workflow>, dataset: String): List<Result> {
        throw UnsupportedOperationException()
    }
}
