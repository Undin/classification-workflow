package com.warrior.classification_workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.warrior.classification_workflow.core.Workflow
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.core.meta.entity.ClassifierPerformanceEntity
import com.warrior.classification_workflow.core.meta.entity.MetaFeaturesEntity
import com.warrior.classification_workflow.core.meta.entity.TransformerPerformanceEntity
import com.warrior.classification_workflow.meta.AlgorithmChooser
import libsvm.svm
import org.apache.commons.cli.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.Supplier
import weka.classifiers.Evaluation
import weka.core.Instances
import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Created by warrior on 29/06/16.
 */
private const val RESULT_FOLDER = "results/master"
private val logger = lazy { LogManager.getLogger("main") }

fun main(args: Array<String>) {
    val configPath = parseArgs(args)
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    val config: Config = yamlMapper.readValue(File(configPath))

    val algorithmChooser = algorithmChooser(config)
    val instances = load("${config.datasetFolder}/${config.dataset}")

    instances.randomize(Random())
    val train = instances.trainCV(4, 0)
    val test = instances.testCV(4, 0)

    val computationManager = computationManager(config, algorithmChooser, train)
    val ga = GeneticAlgorithm(config, computationManager)
    svm.svm_set_print_string_function { it -> }

    val time = measureTimeMillis {
        val result = ga.search()
        val testScore = testWorkflow(result.workflow, train, test)
        val datasetName = instances.relationName()

        val mapper = jacksonObjectMapper()
        val workflowPerformanceEntity = WorkflowPerformanceEntity(
                datasetName = datasetName,
                workflow = result.workflow,
                trainScore = result.measure,
                testScore = testScore)
        File(RESULT_FOLDER).mkdirs()
        mapper.writeValue(File(RESULT_FOLDER, "master-$datasetName.json"), workflowPerformanceEntity)

        val loggerInstance = logger.value
        loggerInstance.info(Supplier { result.workflow })

        loggerInstance.info("train score: ${result.measure}")
        loggerInstance.info("test score: $testScore")
    }
    logger.value.info("computation time: $time")
}

private fun testWorkflow(workflow: Workflow, train: Instances, test: Instances): Double {
    val classifier = workflow.classifier()
    val eval = Evaluation(train)
    classifier.buildClassifier(train)
    eval.evaluateModel(classifier, test)
    return eval.unweightedMacroFmeasure()
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

private fun algorithmChooser(config: Config): AlgorithmChooser {
    val jsonMapper = jacksonObjectMapper()
    val paths = config.metaDataPaths
    val metaFeatures: List<MetaFeaturesEntity> = jsonMapper.readValue(File(paths.metaFeaturesPath))
    val classifierPerformance: List<ClassifierPerformanceEntity> = jsonMapper.readValue(File(paths.classifierPerformancePath))
    val transformerPerformance: List<TransformerPerformanceEntity> = jsonMapper.readValue(File(paths.transformerPerformancePath))

    val datasets = metaFeatures.mapTo(HashSet()) { it.datasetName }
    datasets.remove(config.dataset.removeSuffix(".arff"))

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

private fun parseArgs(args: Array<String>): String? {
    val configOption = Option.builder("c")
            .longOpt("config")
            .hasArg(true)
            .argName("path")
            .desc("path to config_name.yaml file")
            .build()
    val helpOption = Option.builder("h")
            .longOpt("help")
            .desc("show this help")
            .build()
    val allOptions = options(configOption, helpOption)

    val parser = DefaultParser()
    val line = try {
        parser.parse(options(helpOption), args, false)
    } catch (e: ParseException) {
        null
    }
    if (line != null && line.hasOption(helpOption.opt)) {
        printHelp(allOptions)
        System.exit(0)
    } else {
        try {
            val configOptions = options(configOption)
            val line = parser.parse(configOptions, args)
            if (line.hasOption(configOption.opt)) {
                return line.getOptionValue(configOption.opt)
            } else {
                printHelp(allOptions)
                System.exit(0)
            }
        } catch (e: ParseException) {
            logger.value.error(e.message, e)
            printHelp(allOptions)
            System.exit(1)
        }
    }
    // unreachable
    return ""
}

private fun options(vararg opts: Option): Options {
    val options = Options()
    for (opt in opts) {
        options.addOption(opt)
    }
    return options
}

private fun printHelp(options: Options) {
    val formatter = HelpFormatter()
    formatter.printHelp("java -jar jarfile.jar [options...]", options)
}
