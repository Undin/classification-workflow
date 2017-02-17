package com.warrior.classification_workflow.baseline.params

/**
 * Created by warrior on 2/17/17.
 */
internal class BoundExpressionVisitor : BaseExpressionVisitor() {
    override fun visitStart(ctx: ParamsParser.StartContext): Expression {
        throw UnsupportedOperationException("'start' variable is not allowed here")
    }

    override fun visitEnd(ctx: ParamsParser.EndContext): Expression {
        throw UnsupportedOperationException("'end' variable is not allowed here")
    }
}