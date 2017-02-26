package com.warrior.classification_workflow

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.warrior.classification_workflow.core.Workflow
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.core.meta.entity.ClassifierPerformanceEntity
import com.warrior.classification_workflow.core.meta.entity.MetaFeaturesEntity
import com.warrior.classification_workflow.core.meta.entity.TransformerPerformanceEntity
import com.warrior.classification_workflow.meta.AlgorithmChooser
import libsvm.svm
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.Supplier
import weka.classifiers.Evaluation
import weka.core.Instances
import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

class WorkflowConstructor(private val config: Config) {

    private val logger = LogManager.getLogger(WorkflowConstructor::class.java)

    fun construct(datasetName: String, trainDataset: String, testDataset: String) {
        val train = load("${config.datasetFolder}/$trainDataset")
        train.setRelationName(datasetName)
        val test = load("${config.datasetFolder}/$testDataset")
        test.setRelationName(datasetName)

        construct(datasetName, train, test)
    }

    fun construct(dataset: String) {
        val index = dataset.lastIndexOf('.')
        val datasetName = dataset.substring(0, index)
        val instances = load("${config.datasetFolder}/$dataset")

        instances.randomize(Random())
        val train = instances.trainCV(4, 0)
        val test = instances.testCV(4, 0)

        construct(datasetName, train, test)
    }

    private fun construct(datasetName: String, train: Instances, test: Instances) {
        val algorithmChooser = algorithmChooser(config, datasetName)
        val computationManager = computationManager(config, algorithmChooser, train)
        val ga = GeneticAlgorithm(config, computationManager)
        svm.svm_set_print_string_function { it -> }

        val time = measureTimeMillis {
            val result = ga.search(datasetName)
            val testScore = testWorkflow(result.workflow, train, test)

            val mapper = jacksonObjectMapper()
            val workflowPerformanceEntity = WorkflowPerformanceEntity(
                    datasetName = datasetName,
                    workflow = result.workflow,
                    trainScore = result.measure,
                    testScore = testScore)
            File(config.outFolder).mkdirs()
            mapper.writeValue(File(config.outFolder, "master-$datasetName.json"), workflowPerformanceEntity)

            logger.info(Supplier { result.workflow })
            logger.info("train score: ${result.measure}")
            logger.info("test score: $testScore")
        }
        logger.info("computation time: $time")
    }

    private fun algorithmChooser(config: Config, datasetName: String): AlgorithmChooser {
        val jsonMapper = jacksonObjectMapper()
        val paths = config.metaDataPaths
        val metaFeatures: List<MetaFeaturesEntity> = jsonMapper.readValue(File(paths.metaFeaturesPath))
        val classifierPerformance: List<ClassifierPerformanceEntity> = jsonMapper.readValue(File(paths.classifierPerformancePath))
        val transformerPerformance: List<TransformerPerformanceEntity> = jsonMapper.readValue(File(paths.transformerPerformancePath))

        val datasets = metaFeatures.mapTo(HashSet()) { it.datasetName }
        datasets.remove(datasetName)

        val classifiers = classifierPerformance.mapTo(HashSet()) { it.classifierName }
        val transformers = transformerPerformance.mapTo(HashSet()) { it.transformerName }

        val algorithmChooser = AlgorithmChooser(
                datasets = datasets,
                classifiers = classifiers,
                transformers = transformers,
                metaFeatures = metaFeatures,
                classifierPerformance = classifierPerformance,
                transformerPerformance = transformerPerformance
        )
        return algorithmChooser
    }

    private fun computationManager(config: Config, algorithmChooser: AlgorithmChooser, instances: Instances): LocalComputationManager {
        val classifierMap = config.classifiers.associateBy { it.name }
        val transformerMap = config.transformers.associateBy { it.name }
        val cache: Cache<String, MutableMap<Int, Instances>> = Caffeine.newBuilder()
                .maximumSize((config.params.populationSize + 1) * config.params.mutationNumber.toLong())
                .build()

        val computationManager = LocalComputationManager(
                instances = instances,
                algorithmChooser = algorithmChooser,
                classifiersMap = classifierMap,
                transformersMap = transformerMap,
                cache = cache,
                cachePrefixSize = config.cachePrefixSize,
                threads = config.threads
        )
        return computationManager
    }

    private fun testWorkflow(workflow: Workflow, train: Instances, test: Instances): Double {
        val classifier = workflow.classifier()
        val eval = Evaluation(train)
        classifier.buildClassifier(train)
        eval.evaluateModel(classifier, test)
        return eval.unweightedMacroFmeasure()
    }
}
