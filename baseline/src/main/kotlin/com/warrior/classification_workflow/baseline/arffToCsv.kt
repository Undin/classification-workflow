package com.warrior.classification_workflow.baseline

import com.warrior.classification_workflow.core.load
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.QuoteMode
import java.io.File
import java.io.PrintWriter

/**
 * Created by warrior on 1/22/17.
 */
private const val CSV_FOLDER = "datasets/csv"

fun main(args: Array<String>) {
    File(CSV_FOLDER).mkdirs()
    File("datasets").listFiles()?.let { files ->
        files.asSequence()
                .filter { it.exists() && it.extension == "arff" }
                .forEach { dataset ->
                    val instances = load(dataset.absolutePath, false)
                    println(instances.relationName())
                    instances.setClassIndex(-1)
                    val attrs = instances.enumerateAttributes().toList().map { it.name() }.toTypedArray()
                    val writer = PrintWriter("$CSV_FOLDER/${dataset.nameWithoutExtension}.csv")
                    val csvPrinter = CSVFormat.DEFAULT
                            .withHeader(*attrs)
                            .withQuoteMode(QuoteMode.NON_NUMERIC)
                            .withQuote('"')
                            .withDelimiter(';')
                            .print(writer)
                    csvPrinter.use { printer ->
                        for (instance in instances) {
                            val values = Array<Any>(instance.numAttributes()) { i ->
                                val attr = instance.attribute(i)
                                if (attr.isNumeric) instance.value(attr) else instance.value(attr).toInt()
                            }
                            printer.printRecords(values)
                        }
                    }
                }
    }
}
