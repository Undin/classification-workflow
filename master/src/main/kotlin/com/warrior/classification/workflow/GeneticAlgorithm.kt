package com.warrior.classification.workflow

import com.warrior.classification.workflow.core.Algorithm
import com.warrior.classification.workflow.core.ComputationManager
import com.warrior.classification.workflow.core.Config
import com.warrior.classification.workflow.core.Workflow
import java.util.*

/**
 * Created by warrior on 26/04/16.
 */
class GeneticAlgorithm(
        val config: Config,
        val computationManager: ComputationManager,
        val random: Random = Random()
) {

    var maxSize = 10
    var survivedPart = 0.1
    var tournamentProbability = 0.8
    var pointCrossoverProbability = 0.5

    private val algorithms = config.classifiers + config.transformers

    fun search(populationSize: Int, iteration: Int): Individual {
        val initialAlgorithms = (1..populationSize).map { generate() }
        var population = computationManager.compute(initialAlgorithms, config.dataset)
                .map { result -> Individual(result.workflow, result.measure) }
                .sortedDescending()

        for (i in 1..iteration) {
            val childrenWorkflows = (1..populationSize).flatMap {
                val (first, second) = generateParents(population)
                crossover(first.workflow, second.workflow)
            }.map { mutation(it) }

            val children = computationManager.compute(childrenWorkflows, config.dataset)
                    .map { result -> Individual(result.workflow, result.measure) }
            population = selection(population, children)
            println("--- iteration $i ---")
            population.forEach { println(it.result) }
        }
        return population[0]
    }

    private fun generateParents(population: List<Individual>): Pair<Individual, Individual> {
        val first = population.randomElement()
        val second = population.randomElement()
        return Pair(first, second)
    }

    private fun generate(): Workflow {
        val size = random.nextInt(maxSize - 1)
        val flow = (1..size).map { algorithms.randomElement() }
        val classifier = config.classifiers.randomElement()
        return Workflow(flow, classifier)
    }

    private fun crossover(first: Workflow, second: Workflow): List<Workflow> {
        return if (random.nextDouble() < pointCrossoverProbability) {
            pointCrossover(first, second)
        } else {
            intervalCrossover(first, second)
        }
    }

    private fun pointCrossover(first: Workflow, second: Workflow): List<Workflow> {
        val firstAlgorithms = first.allAlgorithms
        val secondAlgorithms = second.allAlgorithms

        var firstIndex = random.nextInt(firstAlgorithms.size)
        var secondIndex = random.nextInt(secondAlgorithms.size)

        while (firstIndex == firstAlgorithms.lastIndex && secondAlgorithms[secondIndex] !is Algorithm.Classifier ||
                secondIndex == secondAlgorithms.lastIndex && firstAlgorithms[firstIndex] !is Algorithm.Classifier) {
            if (firstIndex == firstAlgorithms.lastIndex) {
                secondIndex = random.nextInt(secondAlgorithms.size)
            } else {
                firstIndex = random.nextInt(firstAlgorithms.size)
            }
        }

        val newFirstAlgorithms = ArrayList(firstAlgorithms)
        val newSecondAlgorithms = ArrayList(secondAlgorithms)
        newFirstAlgorithms[firstIndex] = secondAlgorithms[secondIndex]
        newSecondAlgorithms[secondIndex] = firstAlgorithms[firstIndex]

        val newFirst = Workflow(newFirstAlgorithms)
        val newSecond = Workflow(newSecondAlgorithms)

        return listOf(newFirst, newSecond)
    }

    private fun intervalCrossover(first: Workflow, second: Workflow): List<Workflow> {
        val firstAlgorithms = first.allAlgorithms
        val secondAlgorithms = second.allAlgorithms

        var firstRange = randomRange(firstAlgorithms.size)
        var secondRange = randomRange(secondAlgorithms.size)

        while (firstRange.endInclusive == firstAlgorithms.lastIndex && secondAlgorithms[secondRange.endInclusive] !is Algorithm.Classifier ||
               secondRange.endInclusive == secondAlgorithms.lastIndex && firstAlgorithms[firstRange.endInclusive] !is Algorithm.Classifier) {
            if (firstRange.endInclusive == firstAlgorithms.lastIndex) {
                secondRange = randomRange(secondAlgorithms.size)
            } else {
                firstRange = randomRange(firstAlgorithms.size)
            }
        }

        val newFirstAlgorithms = ArrayList<Algorithm>(firstAlgorithms.size - firstRange.length() + secondRange.length())
        val newSecondAlgorithms = ArrayList<Algorithm>(secondAlgorithms.size - secondRange.length() + firstRange.length())

        newFirstAlgorithms += firstAlgorithms.subList(0, firstRange.start)
        newFirstAlgorithms += secondAlgorithms.subList(secondRange.start, secondRange.endInclusive + 1)
        newFirstAlgorithms += firstAlgorithms.subList(firstRange.endInclusive + 1, firstAlgorithms.size)
        newSecondAlgorithms += secondAlgorithms.subList(0, secondRange.start)
        newSecondAlgorithms += firstAlgorithms.subList(firstRange.start, firstRange.endInclusive + 1)
        newSecondAlgorithms += secondAlgorithms.subList(secondRange.endInclusive + 1, secondAlgorithms.size)

        val newFirst = Workflow(newFirstAlgorithms)
        val newSecond = Workflow(newSecondAlgorithms)

        return listOf(newFirst, newSecond)
    }

    private fun mutation(workflow: Workflow): Workflow {
        return pointStructureMutation(workflow)
    }

    private fun pointStructureMutation(workflow: Workflow): Workflow {
        val newAlgorithms = ArrayList(workflow.allAlgorithms)
        val index = random.nextInt(newAlgorithms.size)
        newAlgorithms[index] = if (index == newAlgorithms.lastIndex) { config.classifiers.randomElement() } else { algorithms.randomElement() }
        return Workflow(newAlgorithms)
    }

    private fun selection(currentPopulation: List<Individual>, children: List<Individual>): List<Individual> {
        val size = currentPopulation.size
        val newPopulation = ArrayList<Individual>(size)
        val survivedCount = Math.max(1.0, size * survivedPart).toInt()
        newPopulation += currentPopulation.subList(0, survivedCount)
        val other = ArrayList<Individual>(size - survivedCount + children.size)
        other += currentPopulation.subList(survivedCount, size)
        other += children

        while (newPopulation.size < size) {
            var first = random.nextInt(other.size)
            var second = random.nextInt(other.size)
            if (other[first] < other[second]) {
                val c = first
                first = second
                second = c
            }
            val index = if (random.nextDouble() < tournamentProbability) { first } else { second }
            newPopulation.add(other.removeAt(index))
        }
        return newPopulation.sortedDescending()
    }

    private fun <T> List<T>.randomElement(): T = get(random.nextInt(size))

    private fun randomRange(bound: Int): IntRange {
        var left = random.nextInt(bound)
        var right = random.nextInt(bound)

        if (left > right) {
            val c = left
            left = right
            right = c
        }
        return IntRange(left, right)
    }

    private fun IntRange.length(): Int = endInclusive - start + 1

    data class Individual(val workflow: Workflow, var result: Double): Comparable<Individual> {
        override operator fun compareTo(other: Individual): Int = result.compareTo(other.result)
    }
}



