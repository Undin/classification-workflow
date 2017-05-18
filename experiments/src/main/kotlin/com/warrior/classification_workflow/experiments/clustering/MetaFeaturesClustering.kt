package com.warrior.classification_workflow.experiments.clustering

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.warrior.classification_workflow.core.meta.entity.MetaFeaturesEntity
import org.apache.commons.cli.*
import weka.clusterers.AbstractClusterer
import weka.clusterers.DBSCAN
import weka.clusterers.XMeans
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.filters.Filter
import weka.filters.unsupervised.attribute.Normalize
import java.io.File

/**
 * Created by warrior on 12/6/16.
 */
fun main(args: Array<String>) {
    val path = parseArgs(args)

    val mapper = ObjectMapper().registerKotlinModule()
    val metaFeatures: List<MetaFeaturesEntity> = mapper.readValue(File(path))

    val attributes = arrayListOf(
            Attribute("number_of_instances"),
            Attribute("number_of_features"),
            Attribute("number_of_classes"),
            Attribute("dataset_dimensionality"),
            Attribute("mean_coefficient_of_variation"),
            Attribute("mean_kurtosis"),
            Attribute("mean_skewness"),
            Attribute("mean_standard_deviation"),
            Attribute("equivalent_number_of_features"),
            Attribute("max_mutual_information"),
            Attribute("mean_mutual_information"),
            Attribute("mean_normalized_feature_entropy"),
            Attribute("noise_signal_ratio"),
            Attribute("normalized_class_entropy")
    )
    val instances = Instances("meta-features", attributes, metaFeatures.size)
    for (m in metaFeatures) {
        instances += DenseInstance(1.0, m.toDoubleArray())
    }

    val normalize = Normalize()
    normalize.setInputFormat(instances)
    val normalizedInstances = Filter.useFilter(instances, normalize)

    val clusterers = listOf<AbstractClusterer>(
            DBSCAN().apply {
                epsilon = 0.35
            },
            XMeans()
    )

    for (clusterer in clusterers) {
        clusterer.buildClusterer(normalizedInstances)
        println("${clusterer.javaClass.simpleName}: ${clusterer.numberOfClusters()}")
    }
}

private fun parseArgs(args: Array<String>): String {
    val metaFeatureFileOptions = Option.builder("m")
            .longOpt("meta-features")
            .hasArg(true)
            .argName("path")
            .desc("path to extracted meta-features file-name.json")
            .build()
    val helpOption = Option.builder("h")
            .longOpt("help")
            .desc("show this help")
            .build()
    val allOptions = options(metaFeatureFileOptions, helpOption)

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
            val configOptions = options(metaFeatureFileOptions)
            val line = parser.parse(configOptions, args)
            return line.getOptionValue(metaFeatureFileOptions.opt)
        } catch (e: ParseException) {
            println(e.message)
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
