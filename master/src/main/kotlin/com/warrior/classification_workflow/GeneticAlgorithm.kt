package com.warrior.classification_workflow

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.warrior.classification.workflow.core.*
import com.warrior.classification_workflow.core.Algorithm
import com.warrior.classification_workflow.core.AlgorithmConfiguration.ClassifierConfiguration
import com.warrior.classification_workflow.core.AlgorithmConfiguration.TransformerConfiguration
import com.warrior.classification_workflow.core.ComputationManager
import com.warrior.classification_workflow.core.Result
import com.warrior.classification_workflow.core.Workflow
import java.io.File
import java.io.PrintWriter
import java.util.*

/**
 * Created by warrior on 26/04/16.
 */
class GeneticAlgorithm(
        val config: Config,
        val computationManager: ComputationManager,
        val random: Random = Random()
) {

    private val algorithms = config.classifiers + config.transformers
    private val classifiers: Map<String, ClassifierConfiguration>
    private val transformers: Map<String, TransformerConfiguration>

    init {
        classifiers = HashMap(config.classifiers.size)
        for (c in config.classifiers) {
            classifiers[c.name] = c
        }
        transformers = HashMap(config.transformers.size)
        for (t in config.transformers) {
            transformers[t.name] = t
        }
    }

    fun search(): Result {
        val logs = File(config.logFolder, "${config.dataset}-${System.currentTimeMillis()}")
        logs.mkdir()

        val mapper = ObjectMapper()
        mapper.disable(MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS)

        val initialAlgorithms = (1..config.populationSize).map { generate() }
        var population = computationManager.compute(initialAlgorithms, config.dataset)
                .sortedDescending()
        writeLogs(population, logs, 0, mapper)

        for (i in 1..config.generations) {
            val childrenWorkflows = (1..config.populationSize).flatMap {
                val (first, second) = generateParents(population)
                crossover(first.workflow, second.workflow)
            }.map { mutation(it) }

            val children = computationManager.compute(childrenWorkflows, config.dataset)
            population = selection(population, children)

            writeLogs(population, logs, i, mapper)
        }
        return population[0]
    }

    private fun writeLogs(population: List<Result>, logs: File, iteration: Int, mapper: ObjectMapper) {
        PrintWriter(File(logs, "$iteration.json")).use {
            mapper.writeValue(it, population)
        }
        println("--- iteration $iteration ---")
        population.forEach {
            println(it.measure)
            println(mapper.writeValueAsString(it.workflow))
        }
    }

    private fun generateParents(population: List<Result>): Pair<Result, Result> {
        val first = population.randomElement(random)
        val second = population.randomElement(random)
        return Pair(first, second)
    }

    private fun generate(): Workflow {
        val size = random.nextInt(config.maxWorkflowSize - 1)
        val flow = (1..size).map { algorithms.randomElement(random).randomAlgorithm(random) }
        val classifier = config.classifiers.randomElement(random).randomClassifier(random)
        return Workflow(flow, classifier)
    }

    private fun crossover(first: Workflow, second: Workflow): List<Workflow> {
        return if (random.nextDouble() < config.pointCrossoverProbability) {
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

        var firstRange = randomRange(firstAlgorithms.size, random)
        var secondRange = randomRange(secondAlgorithms.size, random)

        while (firstRange.endInclusive == firstAlgorithms.lastIndex && secondAlgorithms[secondRange.endInclusive] !is Algorithm.Classifier ||
               secondRange.endInclusive == secondAlgorithms.lastIndex && firstAlgorithms[firstRange.endInclusive] !is Algorithm.Classifier) {
            if (firstRange.endInclusive == firstAlgorithms.lastIndex) {
                secondRange = randomRange(secondAlgorithms.size, random)
            } else {
                firstRange = randomRange(firstAlgorithms.size, random)
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
        if (random.nextDouble() < config.mutationProbability) {
            return if (random.nextDouble() < config.structureMutationProbability) {
                pointStructureMutation(workflow)
            } else {
                paramMutation(workflow)
            }
        }
        return workflow
    }

    private fun pointStructureMutation(workflow: Workflow): Workflow {
        val newAlgorithms = ArrayList(workflow.allAlgorithms)
        val index = random.nextInt(newAlgorithms.size)
        val configuration = if (index == newAlgorithms.lastIndex) { config.classifiers } else { algorithms }
        newAlgorithms[index] = configuration.randomElement(random).randomAlgorithm(random)
        return Workflow(newAlgorithms)
    }

    private fun paramMutation(workflow: Workflow): Workflow {
        val newAlgorithms = ArrayList(workflow.allAlgorithms)
        val index = random.nextInt(newAlgorithms.size)
        val algo = newAlgorithms[index]
        newAlgorithms[index] = when (algo) {
            is Algorithm.Classifier -> {
                val classifierConfiguration = classifiers[algo.name]!!
                classifierConfiguration.randomClassifier(random)
            }
            is Algorithm.Transformer -> {
                val transformerConfiguration = transformers[algo.name]!!
                transformerConfiguration.randomTransformer(random)
            }
        }
        return Workflow(newAlgorithms)
    }

    private fun selection(currentPopulation: List<Result>, children: List<Result>): List<Result> {
        val size = currentPopulation.size
        val newPopulation = ArrayList<Result>(size)
        val survivedCount = Math.max(1.0, size * config.survivedPart).toInt()
        newPopulation += currentPopulation.subList(0, survivedCount)
        val other = ArrayList<Result>(size - survivedCount + children.size)
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
            val index = if (random.nextDouble() < config.tournamentProbability) { first } else { second }
            newPopulation.add(other.removeAt(index))
        }
        return newPopulation.sortedDescending()
    }

    private fun IntRange.length(): Int = endInclusive - start + 1
}
