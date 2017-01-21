package com.warrior.classification_workflow

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.warrior.classification_workflow.ComputationManager.Mutation.HyperparamMutation
import com.warrior.classification_workflow.ComputationManager.Mutation.StructureMutation
import com.warrior.classification_workflow.core.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.Supplier
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

    private val logger = LogManager.getLogger(GeneticAlgorithm::class.java)
    private val mapper = jacksonObjectMapper()

    private val params: GeneticAlgorithmParams = config.params
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

        val initialWorkflows = generate()
        var population = computationManager.evaluate(initialWorkflows)
                .sortedDescending()
        writeLogs(population, logs, 0)

        for (i in 1..params.generations) {
            val mutations = mutations(population)
            val mutationResults = computationManager.evaluate(mutations)

            val newPopulation = ArrayList<Result>(population.size)
            for ((j, individual) in population.withIndex()) {
                var bestResult = individual
                for (k in params.mutationNumber * j until params.mutationNumber * (j + 1)) {
                    if (mutationResults[k] > bestResult) {
                        bestResult = mutationResults[k]
                    }
                }
                newPopulation += bestResult
            }
            population = newPopulation.sortedDescending()
            writeLogs(population, logs, i)
        }

        return population[0]
    }

    private fun writeLogs(population: List<Result>, logs: File, iteration: Int) {
        PrintWriter(File(logs, "$iteration.json")).use {
            mapper.writeValue(it, population)
        }
        logger.info(Supplier {
            val builder = StringBuilder()
            builder.append("--- iteration $iteration ---\n")
            population.forEach {
                builder.append(it.measure)
                        .append("\n")
                        .append(mapper.writeValueAsString(it.workflow))
                        .append("\n")
            }
            builder.toString()
        })
    }

    private fun generateParents(population: List<Result>): Pair<Result, Result> {
        val first = population.randomElement(random)
        val second = population.randomElement(random)
        return Pair(first, second)
    }

    private fun generate(): List<Workflow> {
        val sizes = (1..params.populationSize).map { random.nextInt(params.maxWorkflowSize) + 1 }
        return computationManager.generate(params.populationSize, sizes)
    }

    private fun crossover(first: Workflow, second: Workflow): List<Workflow> {
        return if (random.nextDouble() < params.pointCrossoverProbability) {
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

        while (firstIndex == firstAlgorithms.lastIndex && secondAlgorithms[secondIndex] !is Classifier ||
                secondIndex == secondAlgorithms.lastIndex && firstAlgorithms[firstIndex] !is Classifier) {
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

        while (firstRange.endInclusive == firstAlgorithms.lastIndex && secondAlgorithms[secondRange.endInclusive] !is Classifier ||
               secondRange.endInclusive == secondAlgorithms.lastIndex && firstAlgorithms[firstRange.endInclusive] !is Classifier) {
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
        if (random.nextDouble() < params.mutationProbability) {
            return if (random.nextDouble() < params.structureMutationProbability) {
                pointStructureMutation(workflow)
            } else {
                paramMutation(workflow)
            }
        }
        return workflow
    }

    private fun mutations(population: List<Result>): List<Workflow> {
        val mutationParams = population.flatMapTo(ArrayList(population.size * params.mutationNumber)) { individual ->
            val workflow = individual.workflow
            val size = workflow.algorithms.size
            (1..params.mutationNumber).map {
                if (random.nextDouble() < params.structureMutationProbability) {
                    val keepPrefixSize = random.nextInt(size)
                    val nextSize = random.nextInt(params.maxWorkflowSize - keepPrefixSize) + keepPrefixSize + 1
                    StructureMutation(workflow, keepPrefixSize, nextSize)
                } else {
                    HyperparamMutation(workflow)
                }
            }
        }
        return computationManager.mutation(mutationParams)
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
        for ((index, algo) in newAlgorithms.withIndex()) {
            if (random.nextDouble() < 1.0 / newAlgorithms.size) {
                newAlgorithms[index] = when (algo) {
                    is Classifier -> {
                        val classifierConfiguration = classifiers[algo.name]!!
                        classifierConfiguration.randomClassifier(random)
                    }
                    is Transformer -> {
                        val transformerConfiguration = transformers[algo.name]!!
                        transformerConfiguration.randomTransformer(random)
                    }
                    else -> throw UnsupportedOperationException()
                }
            }
        }
        return Workflow(newAlgorithms)
    }

    private fun selection(currentPopulation: List<Result>, children: List<Result>): List<Result> {
        val size = currentPopulation.size
        val newPopulation = ArrayList<Result>(size)
        val survivedCount = Math.max(1.0, size * params.survivedPart).toInt()
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
            val index = if (random.nextDouble() < params.tournamentProbability) { first } else { second }
            newPopulation.add(other.removeAt(index))
        }
        return newPopulation.sortedDescending()
    }

    private fun IntRange.length(): Int = endInclusive - start + 1
}
