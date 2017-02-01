package com.warrior.classification_workflow.baseline.single

/**
 * Created by warrior on 2/1/17.
 */
class DoubleProgression(
        private val start: Double,
        private val end: Double,
        private val step: Double
) : Iterable<Double> {
    override fun iterator(): Iterator<Double> = DoubleProgressionIterator(start, end, step)
}

open class DoubleProgressionIterator(
        private val start: Double,
        private val end: Double,
        private val step: Double
) : Iterator<Double> {

    private var next = start
    private var hasNext: Boolean = start < end

    override fun hasNext(): Boolean = hasNext

    override fun next(): Double {
        val value = next
        if (value >= end) {
            hasNext = false
        } else {
            next += step
        }
        return value
    }
}
