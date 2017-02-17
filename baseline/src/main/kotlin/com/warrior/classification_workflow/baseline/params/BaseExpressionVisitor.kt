package com.warrior.classification_workflow.baseline.params

import com.warrior.classification_workflow.baseline.params.Expression.*
import com.warrior.classification_workflow.baseline.params.ParamsLexer.*

/**
 * Created by warrior on 2/17/17.
 */
internal abstract class BaseExpressionVisitor : ParamsBaseVisitor<Expression>() {

    override fun visitExpression(ctx: ParamsParser.ExpressionContext): Expression {
        return if (ctx.op != null) {
            val left = visitExpression(ctx.left)
            val right = visitExpression(ctx.right)
            when (ctx.op.type) {
                MUL -> Binary.Mul(left, right)
                DIV -> Binary.Div(left, right)
                ADD -> Binary.Add(left, right)
                SUB -> Binary.Sub(left, right)
                else -> throw IllegalStateException("unreachable state")
            }
        } else {
            super.visitExpression(ctx)
        }
    }

    override fun visitPrimary(ctx: ParamsParser.PrimaryContext): Expression {
        return if (ctx.expression() != null) {
            visitExpression(ctx.expression())
        } else {
            super.visitPrimary(ctx)
        }
    }

    override fun visitUnaryMinus(ctx: ParamsParser.UnaryMinusContext): UnaryMinus {
        return UnaryMinus(visitExpression(ctx.expression()))
    }

    override fun visitFunction(ctx: ParamsParser.FunctionContext): Expression {
        val argument = visitExpression(ctx.expression())
        return when (ctx.`fun`.type) {
            SQRT -> Sqrt(argument)
            ROUND -> Round(argument)
            else -> throw IllegalStateException("unreachable state")
        }
    }

    override fun visitDoubleLiteral(ctx: ParamsParser.DoubleLiteralContext): Const = Const(ctx.text.toDouble())
    override fun visitNumInstances(ctx: ParamsParser.NumInstancesContext): NumInstances = Expression.NumInstances
    override fun visitNumAttributes(ctx: ParamsParser.NumAttributesContext): NumAttributes = Expression.NumAttributes

    override abstract fun visitStart(ctx: ParamsParser.StartContext): Expression
    override abstract fun visitEnd(ctx: ParamsParser.EndContext): Expression
}
