package com.warrior.classification_workflow

import com.github.benmanes.caffeine.cache.Cache
import com.warrior.classification_workflow.core.*
import com.warrior.classification_workflow.ComputationManager.Mutation.*
import com.warrior.classification_workflow.core.meta.features.CommonMetaFeatureExtractor
import com.warrior.classification_workflow.meta.AlgorithmChooser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import weka.attributeSelection.ASEvaluation
import weka.attributeSelection.ASSearch
import weka.attributeSelection.Ranker
import weka.classifiers.AbstractClassifier
import weka.classifiers.evaluation.Evaluation
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.filters.Filter
import weka.filters.supervised.attribute.AttributeSelection
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger
import kotlin.streams.toList

/**
 * Created by warrior on 29/06/16.
 */
class LocalComputationManager(
        instances: Instances,
        private val algorithmChooser: AlgorithmChooser,
        private val classifiersMap: Map<String, ClassifierConfiguration>,
        private val transformersMap: Map<String, TransformerConfiguration>,
        private val cache: Cache<String, MutableMap<Int, Instances>>,
        private val cachePrefixSize: Int,
        threads: Int
) : ComputationManager {

    private val MAX_INSTANCES = 10000

    private val logger: Logger = LogManager.getLogger(LocalComputationManager::class.java)

    private val algorithmsMap: Map<String, AlgorithmConfiguration> = classifiersMap + transformersMap

    private val numFolds: Int = 10
    private val random: Random = Random()
    private val pool: ForkJoinPool = ForkJoinPool(threads)

    private val parts: Int = 5
    private val train: Instances
    private val test: Instances

    init {
        val subInstances = subInstances(instances, MAX_INSTANCES, random)
        subInstances.randomize(random)
        train = subInstances.trainCV(parts, 0)
        test = subInstances.testCV(parts, 0)
    }

    override fun generate(count: Int, sizes: List<Int>): List<Workflow> {
        return submit {
            sizes.parallelStream()
                    .map { size ->
                        generateSuffix(randomUUID(), train, ArrayList(), size, CommonMetaFeatureExtractor())
                    }
                    .toList()
        }
    }

    override fun mutation(params: List<ComputationManager.Mutation>): List<Workflow> {
        return submit {
            params.parallelStream()
                    .map { mutation ->
                        when (mutation) {
                            is StructureMutation -> structureMutation(mutation.workflow, mutation.keepPrefixSize, mutation.size)
                            is HyperparamMutation -> hyperparamMutation(mutation.workflow)
                        }
                    }
                    .toList()
        }
    }

    private fun structureMutation(workflow: Workflow, keepPrefixSize: Int, size: Int): Workflow {
        val extractor = workflow.extractor?.get() ?: CommonMetaFeatureExtractor()
        var data = train

        val uuid = workflow.uuid
        val newUuid = randomUUID()
        val prefix = workflow.algorithms.take(keepPrefixSize)
        val algorithms = ArrayList<Algorithm>(size - 1)
        for ((position, algorithm) in prefix.withIndex()) {
            algorithms += algorithm
            data = algorithm.apply(uuid, position, data)
            toCache(newUuid, position, data)

        }
        return generateSuffix(newUuid, data, algorithms, size, extractor)
    }

    private fun hyperparamMutation(workflow: Workflow): Workflow {
        val newAlgorithms = ArrayList(workflow.allAlgorithms)
        val uuid = workflow.uuid
        val newUuid = randomUUID()
        var isMutated = false
        for ((index, algo) in newAlgorithms.withIndex()) {
            if (random.nextDouble() < 1.0 / newAlgorithms.size) {
                newAlgorithms[index] = when (algo) {
                    is Classifier -> {
                        val classifierConfiguration = classifiersMap[algo.name]!!
                        classifierConfiguration.randomClassifier(random)
                    }
                    is Transformer -> {
                        val transformerConfiguration = transformersMap[algo.name]!!
                        transformerConfiguration.randomTransformer(random)
                    }
                    else -> throw UnsupportedOperationException()
                }
                isMutated = true
            } else if (!isMutated) {
                val data = fromCache(uuid, index)
                if (data != null) {
                    toCache(newUuid, index, data)
                }
            }
        }
        return Workflow(newUuid, newAlgorithms)
    }

    private fun generateSuffix(uuid: String, currentData: Instances, algorithms: MutableList<Algorithm>, size: Int, extractor: CommonMetaFeatureExtractor): Workflow {
        var data = currentData
        for (i in algorithms.size until size) {
            while (true) {
                val algorithmName = algorithmChooser.chooseAlgorithm(extractor, data)
                val algorithm = algorithmsMap[algorithmName]!!.randomAlgorithm(random)
                try {
                    data = algorithm.apply(uuid, i, data)
                    algorithms += algorithm
                    break
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }

        val classifierName = algorithmChooser.chooseClassifier(extractor, data)
        val classifier = classifiersMap[classifierName]!!.randomClassifier(random)
        val workflow = Workflow(uuid, algorithms, classifier, extractor)
        return workflow
    }

    override fun evaluate(workflows: List<Workflow>): List<Result> {
        return submit {
            workflows.parallelStream()
                    .map { w -> Result(w, compute(w, train, test)) }
                    .toList()
        }
    }

    private fun compute(workflow: Workflow, train: Instances, test: Instances): Double {
        val eval = Evaluation(train)
        val classifier = workflow.classifier()
        // FIXME
        try {
            classifier.buildClassifier(train)
            eval.evaluateModel(classifier, test)
        } catch (e: Exception) {
            logger.error(e.message, e)
            return 0.0
        }
        return eval.unweightedMacroFmeasure()
    }

    private fun Algorithm.apply(uuid: String, position: Int, data: Instances): Instances {
        return when (this) {
            is Classifier -> apply(uuid, position, data)
            is Transformer -> apply(uuid, position, data)
            else -> throw UnsupportedOperationException()
        }
    }

    private fun Classifier.apply(uuid: String, position: Int, data: Instances): Instances {
        val cacheData = fromCache(uuid, position)
        return if (cacheData != null) {
            cacheData
        } else {
            // create new Instances container with addition attribute
            val newAttributes = ArrayList<Attribute>(data.numAttributes() + 1)
            val classifierResultAttr = data.classAttribute().copy("$name-${counter.andIncrement}")
            newAttributes += classifierResultAttr
            data.enumerateAttributes()
                    .asSequence()
                    .mapTo(newAttributes) { it.clone() }
            newAttributes += data.classAttribute().clone()
            val newData = Instances(data.relationName(), newAttributes, data.size)
            newData.setClass(newAttributes.last())

            // prepare data for cross validation
            val copiedData = Instances(data)
            copiedData.randomize(random)
            val classifierModel = invoke()
            val classifierCopies = AbstractClassifier.makeCopies(classifierModel, numFolds)

            // cross validation
            for ((i, c) in classifierCopies.withIndex()) {
                val train = copiedData.trainCV(numFolds, i, random)
                val test = copiedData.testCV(numFolds, i)
                c.buildClassifier(train)
                for (inst in test) {
                    val result = c.classifyInstance(inst)
                    val values = DoubleArray(newAttributes.size) { j -> if (j == 0) result else inst.value(j - 1) }
                    val newInstance = DenseInstance(1.0, values)
                    newData += newInstance
                }
            }
            toCache(uuid, position, newData)
            newData
        }
    }

    private fun Transformer.apply(uuid: String, position: Int, data: Instances): Instances {
        val cacheData = fromCache(uuid, position)
        return if (cacheData != null) {
            cacheData
        } else {
            val (search, evaluator) = invoke()
            var transformedData = attributeSelection(search, evaluator, data)
            if (transformedData.numAttributes() == 1) {
                if (search is Ranker) {
                    val defaultSearch = Ranker()
                    defaultSearch.numToSelect = 1
                    transformedData = attributeSelection(defaultSearch, evaluator, data)
                } else {
                    logger.warn("attribute selection must choose at least one non class attribute. skip this transformer")
                    transformedData = data
                }
            }
            toCache(uuid, position, transformedData)
            transformedData
        }
    }

    private fun attributeSelection(search: ASSearch, evaluator: ASEvaluation, data: Instances): Instances {
        val selection = AttributeSelection()
        selection.search = search
        selection.evaluator = evaluator
        selection.setInputFormat(data)
        return Filter.useFilter(data, selection)
    }

    private fun fromCache(uuid: String, position: Int): Instances? {
        val workflowCache = cache.get(uuid) { HashMap() }!!
        return workflowCache[position]
    }

    private fun toCache(uuid: String, position: Int, data: Instances) {
        if (position < cachePrefixSize) {
            val workflowCache = cache.get(uuid) { HashMap() }!!
            workflowCache[position] = data
        }
    }

    private fun <T> submit(block: () -> T): T = pool.submit(block).get()

    private fun Attribute.clone(): Attribute = copy(name())

    companion object {
        private val counter = AtomicInteger(0)
    }
}
