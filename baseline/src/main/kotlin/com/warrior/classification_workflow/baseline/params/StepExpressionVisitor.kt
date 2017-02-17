package com.warrior.classification_workflow.baseline.params

import com.warrior.classification_workflow.baseline.params.Expression.*

/**
 * Created by warrior on 2/17/17.
 */
internal class StepExpressionVisitor : BaseExpressionVisitor() {
    override fun visitStart(ctx: ParamsParser.StartContext): Start = Start
    override fun visitEnd(ctx: ParamsParser.EndContext): End = End
}
