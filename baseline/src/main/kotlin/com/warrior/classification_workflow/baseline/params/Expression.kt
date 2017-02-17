package com.warrior.classification_workflow.baseline.params

import java.lang.Math.*

/**
 * Created by warrior on 2/17/17.
 */
internal sealed class Expression {

    sealed class Binary(val left: Expression, val right: Expression) : Expression() {
        class Mul(left: Expression, right: Expression) : Binary(left, right)
        class Div(left: Expression, right: Expression) : Binary(left, right)
        class Add(left: Expression, right: Expression) : Binary(left, right)
        class Sub(left: Expression, right: Expression) : Binary(left, right)
        
        fun apply(leftResult: Double, rightResult: Double): Double = when (this) {
            is Mul -> leftResult * rightResult
            is Div -> leftResult / rightResult
            is Add -> leftResult + rightResult
            is Sub -> leftResult - rightResult
        }
    }
    class Sqrt(val expr: Expression) : Expression()
    class Round(val expr: Expression) : Expression()
    class UnaryMinus(val expr: Expression) : Expression()
    class Const(val value: Double) : Expression()
    object NumInstances : Expression()
    object NumAttributes : Expression()
    object Start : Expression()
    object End : Expression()

    operator fun invoke(numInstances: Int, numAttributes: Int, start: Double, end: Double): Double = when (this) {
        is Binary -> apply(left(numInstances, numAttributes, start, end), right(numInstances, numAttributes, start, end))
        is Sqrt -> sqrt(expr(numInstances, numAttributes, start, end))
        is Round -> round(expr(numInstances, numAttributes, start, end)).toDouble()
        is UnaryMinus -> -expr(numInstances, numAttributes, start, end)
        is Const -> value
        NumInstances -> numInstances.toDouble()
        NumAttributes -> numAttributes.toDouble()
        Start -> start
        End -> end
    }
}
