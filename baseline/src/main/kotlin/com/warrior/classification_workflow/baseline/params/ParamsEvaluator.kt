package com.warrior.classification_workflow.baseline.params

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream

/**
 * Created by warrior on 2/17/17.
 */
class ParamsEvaluator {

    private val boundVisitor: BoundExpressionVisitor = BoundExpressionVisitor()
    private val stepVisitor: StepExpressionVisitor = StepExpressionVisitor()

    fun calculateBound(param: String, numInstances: Int, numAttributes: Int): Double {
        return calculate(boundVisitor, param, numInstances, numAttributes, 0.0, 0.0)
    }

    fun calculateStep(param: String, numInstances: Int, numAttributes: Int,
                      start: Double, end: Double): Double {
        try {
            return calculate(stepVisitor, param, numInstances, numAttributes, start, end)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun calculate(visitor: BaseExpressionVisitor, param: String,
                          numInstances: Int, numAttributes: Int,
                          start: Double, end: Double): Double {
        val stream = ANTLRInputStream(param)
        val lexer = ParamsLexer(stream)
        val tokens = CommonTokenStream(lexer)
        val parser = ParamsParser(tokens)
        val tree = parser.expression()
        val expression = visitor.visit(tree)
        return expression(numInstances, numAttributes, start, end)
    }
}