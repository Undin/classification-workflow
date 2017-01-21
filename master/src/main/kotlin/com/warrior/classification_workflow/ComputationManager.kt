package com.warrior.classification_workflow

import com.warrior.classification_workflow.core.Workflow

/**
 * Created by warrior on 26/04/16.
 */
interface ComputationManager {
    fun generate(count: Int, sizes: List<Int>): List<Workflow>
    fun mutation(params: List<Mutation>): List<Workflow>
    fun evaluate(workflows: List<Workflow>): List<Result>

    sealed class Mutation {
        class StructureMutation(
                val workflow: Workflow,
                val keepPrefixSize: Int,
                val size: Int
        ) : Mutation()

        class HyperparamMutation(val workflow: Workflow) : Mutation()
    }
}
