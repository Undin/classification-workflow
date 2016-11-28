package com.warrior.classification_workflow.meta_learning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.meta_learning.metafeatures.MetaFeatureExtractor
import kotlinx.support.jdk8.collections.parallelStream
import libsvm.svm
import org.apache.commons.cli.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.Supplier
import weka.attributeSelection.ASEvaluation
import weka.attributeSelection.ASSearch
import weka.classifiers.AbstractClassifier
import weka.classifiers.AggregateableEvaluation
import weka.classifiers.Evaluation
import weka.core.Instances
import weka.filters.Filter
import weka.filters.supervised.attribute.AttributeSelection
import java.io.File
import java.util.*
import java.util.stream.IntStream
import java.util.stream.Stream

/**
 * Created by warrior on 11/14/16.
 */
val logger = LogManager.getLogger()

fun main(args: Array<String>) {
    val (metaFeatureConfigPath, classifierPerfConfigPath, transformerPerfConfig) = parseArgs(args)
    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    svm.svm_set_print_string_function { it -> }

    if (metaFeatureConfigPath != null) {
        val config: MetaFeatureConfig = mapper.readValue(File(metaFeatureConfigPath))
        extractMetaFeatures(config)
    }
    if (classifierPerfConfigPath != null) {
        val config: ClassifierPerfConfig = mapper.readValue(File(classifierPerfConfigPath))
        measureClassifierPerformance(config)
    }
    if (transformerPerfConfig != null) {
        val config: TransformerPerfConfig = mapper.readValue(File(transformerPerfConfig))
        measureTransformerPerformance(config)
    }
}

private fun parseArgs(args: Array<String>): ConfigPaths {
    val metaFeatureConfigOption = Option.builder("m")
            .longOpt("meta-feature-config")
            .hasArg(true)
            .argName("path")
            .desc("path to meta-feature extraction config_name.yaml file")
            .build()
    val classifierPerfConfigOption = Option.builder("c")
            .longOpt("classifier-perf-config")
            .hasArg(true)
            .argName("path")
            .desc("path to classifier performance measurement config_name.yaml file")
            .build()
    val transformerPerfConfigOption = Option.builder("t")
            .longOpt("transformer-perf-config")
            .hasArg(true)
            .argName("path")
            .desc("path to transformer performance measurement config_name.yaml file")
            .build()
    val helpOption = Option.builder("h")
            .longOpt("help")
            .desc("show this help")
            .build()
    val allOptions = options(metaFeatureConfigOption, classifierPerfConfigOption,
            transformerPerfConfigOption, helpOption)

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
            val configOptions = options(metaFeatureConfigOption, classifierPerfConfigOption, transformerPerfConfigOption)
            val line = parser.parse(configOptions, args)
            return ConfigPaths(
                    metaFeatureConfigPath = line.getOptionValue(metaFeatureConfigOption.opt),
                    classifierPerfConfigPath = line.getOptionValue(classifierPerfConfigOption.opt),
                    transformerPerfConfigPath = line.getOptionValue(transformerPerfConfigOption.opt)
            )
        } catch (e: ParseException) {
            println(e.message)
            printHelp(allOptions)
            System.exit(1)
        }
    }
    // unreachable
    return ConfigPaths(null, null, null)
}

data class ConfigPaths(
        val metaFeatureConfigPath: String?,
        val classifierPerfConfigPath: String?,
        val transformerPerfConfigPath: String?
)

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

fun measureTransformerPerformance(config: TransformerPerfConfig) {
    val datasets = config.datasets.map { File(config.datasetFolder, it) }
    val saveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)
    val random = Random()

    saveStrategy.use { saveStrategy ->
        datasets.forEachParallel { dataset ->
            val data = load(dataset.absolutePath)
            config.transformers.forEachParallel { transformer ->
                calculate(transformer, config.classifiers, data, random, saveStrategy)
            }
        }
    }
}

fun transform(transformer: Transformer, data: Instances): Instances {
    val (name, search, evaluation) = transformer
    val filter = AttributeSelection()
    filter.search = ASSearch.forName(search.className, search.options.toArray())
    filter.evaluator = ASEvaluation.forName(evaluation.className, evaluation.options.toArray())
    filter.setInputFormat(data)
    return Filter.useFilter(data, filter).apply { setRelationName(data.relationName()) }
}

private fun calculate(transformer: Transformer, classifiers: List<Classifier>, data: Instances,
                      random: Random, saveStrategy: SaveStrategy) {
    val transformedData = withLog("${transformer.name} on ${data.relationName()}") {
        transform(transformer, data)
    }

    classifiers.forEachParallel { classifier ->
        val measure = withLog("evaluate ${classifier.name} on ${data.relationName()}") {
            crossValidation(classifier, transformedData, random)
        }

        val entity = TransformerPerformanceEntity(transformer.name, classifier.name,
                data.relationName(), measure)
        saveStrategy.save(entity)
    }
}

private fun measureClassifierPerformance(config: ClassifierPerfConfig) {
    val datasets = config.datasets.map { File(config.datasetFolder, it) }
    val saveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)

    val random = Random()
    saveStrategy.use { saveStrategy ->
        datasets.forEachParallel { dataset ->
            val data = load(dataset.absolutePath)
            config.classifiers.forEachParallel { calculate(it, data, random, saveStrategy) }
        }
    }
}

private fun calculate(classifier: Classifier, data: Instances, random: Random, saveStrategy: SaveStrategy) {
    val measure = withLog("evaluate ${classifier.name} on ${data.relationName()}") {
        crossValidation(classifier, data, random)
    }

    val entity = ClassifierPerformanceEntity(classifier.name, data.relationName(), measure)
    saveStrategy.save(entity)
}

private fun crossValidation(classifier: Classifier, data: Instances, random: Random): Double {
    val options = classifier.options.toArray()
    val wekaClassifier = AbstractClassifier.forName(classifier.className, options)

    val fullAggregation: AggregateableEvaluation = IntStream.range(0, 10)
            .parallel()
            .boxed()
            .flatMap { parallelCrossValidation(data, wekaClassifier, random, 10) }
            .collect(
                    { AggregateableEvaluation(data) },
                    { acc, o -> acc.aggregate(o) },
                    { l, r -> l.aggregate(r) }
            )
    return fullAggregation.unweightedMacroFmeasure()
}

private fun parallelCrossValidation(data: Instances, classifier: weka.classifiers.Classifier,
                                    random: Random, numFolds: Int): Stream<Evaluation> {
    return IntStream.range(0, numFolds)
            .parallel()
            .mapToObj { fold ->
                val train = data.trainCV(10, fold, random)
                val copiedClassifier = AbstractClassifier.makeCopy(classifier)
                copiedClassifier.buildClassifier(train)
                val test = data.testCV(10, fold)
                val eval = Evaluation(data)
                eval.evaluateModel(copiedClassifier, test)
                eval
            }
}

private fun extractMetaFeatures(config: MetaFeatureConfig) {
    val datasets = config.datasets.map { File(config.datasetFolder, it) }
    val saveStrategy = SaveStrategy.fromString(config.saveStrategy, config.outFolder)

    saveStrategy.use { saveStrategy ->
        datasets.forEachParallel { file ->
            val data = load(file.absolutePath)
            calculate(data, saveStrategy)
        }
    }
}

private fun calculate(data: Instances, saveStrategy: SaveStrategy) {
    val extractor = withLog("extract meta-features: ${data.relationName()}") {
        MetaFeatureExtractor(data)
    }

    val result = extractor.extract()
    val entity = MetaFeaturesEntity(data.relationName(), result)
    saveStrategy.save(entity)
}

private inline fun <T> withLog(message: String, block: () -> T): T {
    logger.info(Supplier { "start $message" })
    val result = block()
    logger.info(Supplier { "end $message" })
    return result
}

private fun Map<String, String>.toArray(): Array<String>
        = flatMap { listOf(it.key, it.value) }.toTypedArray()

private fun <T> Collection<T>.forEachParallel(action: (T) -> Unit)
        = parallelStream().forEach { action(it) }
