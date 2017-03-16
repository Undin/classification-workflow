package com.warrior.classification_workflow.reports

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mongodb.MongoClient
import com.warrior.classification_workflow.WorkflowPerformanceEntity
import com.warrior.classification_workflow.baseline.single.SingleClassifierTuningEntity
import com.warrior.classification_workflow.baseline.tpot.TpotPerformanceEntity
import org.apache.poi.xssf.usermodel.*
import org.jongo.Jongo
import org.jongo.MongoCollection
import org.jongo.marshall.jackson.JacksonMapper
import java.io.FileOutputStream


/**
 * Created by warrior on 3/16/17.
 */
private val DATASETS = listOf(
        "ada_agnostic",
        "adult",
        "agaricus-lepiota",
        "allbp",
        "allhyper",
        "allhypo",
        "allrep",
        "analcatdata_aids",
        "analcatdata_asbestos",
        "analcatdata_authorship",
        "analcatdata_bankruptcy",
        "analcatdata_boxing1",
        "analcatdata_boxing2",
        "analcatdata_creditscore",
        "analcatdata_cyyoung8092",
        "analcatdata_cyyoung9302",
        "analcatdata_dmft",
        "analcatdata_fraud",
        "analcatdata_germangss",
        "analcatdata_happiness",
        "analcatdata_japansolvent",
        "analcatdata_lawsuit",
        "ann-thyroid",
        "anneal",
        "AR10P",
        "australian",
        "auto",
        "backache",
        "balance-scale",
        "biomed",
        "breast",
        "breast-cancer",
        "breast-cancer-wisconsin",
        "breast-w",
        "buggyCrx",
        "calendarDOW",
        "car",
        "car-evaluation",
        "cars",
        "cars1",
        "chess",
        "churn",
        "clean1",
        "clean2",
        "cleve",
        "cleveland",
        "cleveland-nominal",
        "CLL-SUB-111",
        "cloud",
        "cmc",
        "colic",
        "collins",
        "colon",
        "confidence",
        "connect-4",
        "corral",
        "credit-a",
        "credit-g",
        "crx",
        "dermatology",
        "diabetes",
        "dis",
        "dna",
        "ECML90x27679",
        "ecoli",
        "Embryonaldataset_c",
        "flags",
        "flare",
        "GAMETES_Epistasis_2-Way_20atts_0.1H_EDM-1_1",
        "GAMETES_Epistasis_2-Way_20atts_0.4H_EDM-1_1",
        "GAMETES_Epistasis_2-Way_1000atts_0.4H_EDM-1_EDM-1_1",
        "GAMETES_Epistasis_3-Way_20atts_0.2H_EDM-1_1",
        "GAMETES_Heterogeneity_20atts_1600_Het_0.4_0.2_50_EDM-2_001",
        "GAMETES_Heterogeneity_20atts_1600_Het_0.4_0.2_75_EDM-2_001",
        "german",
        "gina_agnostic",
        "gina_prior",
        "gina_prior2",
        "glass",
        "glass2",
        "grub-damage",
        "haberman",
        "hayes-roth",
        "heart-c",
        "heart-h",
        "heart-statlog",
        "hepatitis",
        "Hill_Valley_with_noise",
        "Hill_Valley_without_noise",
        "horse-colic",
        "house-votes-84",
        "hungarian",
        "hypothyroid",
        "ionosphere",
        "iris",
        "irish",
        "kdd_JapaneseVowels",
        "kdd_synthetic_control",
        "kr-vs-kp",
        "krkopt",
        "labor",
        "led7",
        "led24",
        "letter",
        "leukemia",
        "Leukemia_3c",
        "liver-disorder",
        "lupus",
        "lymph",
        "mfeat-factors",
        "mfeat-fourier",
        "mfeat-karhunen",
        "mfeat-morphological",
        "mfeat-pixel",
        "mfeat-zernike",
        "mnist",
        "mofn-3-7-10",
        "molecular-biology_promoters",
        "monk1",
        "monk2",
        "monk3",
        "mushroom",
        "mux6",
        "new-thyroid",
        "nursery",
        "oh0.wc",
        "oh5.wc",
        "oh10.wc",
        "oh15.wc",
        "optdigits",
        "page-blocks",
        "parity5+5",
        "parity5",
        "pasture",
        "pendigits",
        "PIE10P",
        "pima",
        "postoperative-patient-data",
        "prnn_crabs",
        "prnn_fglass",
        "prnn_synth",
        "profb",
        "promoters",
        "satimage",
        "schizo",
        "segmentation",
        "shuttle",
        "sleep",
        "SMK-CAN-187",
        "solar-flare_1",
        "solar-flare_2",
        "sonar",
        "soybean",
        "spambase",
        "spect",
        "spectf",
        "spectrometer",
        "splice",
        "sylva_agnostic",
        "sylva_prior",
        "tae",
        "threeOf9",
        "tic-tac-toe",
        "tokyo1",
        "TOX-171",
        "tr11.wc",
        "tr12.wc",
        "tr23.wc",
        "tr31.wc",
        "tr41.wc",
        "tr45.wc",
        "vehicle",
        "vote",
        "vowel",
        "wap.wc",
        "waveform-21",
        "waveform-40",
        "white-clover",
        "wine-quality-red",
        "wine-quality-white",
        "wine-recognition",
        "xd6",
        "yeast",
        "zoo"
)

fun main(args: Array<String>) {
    val jongo = createJongo("master")

    val workflowCollection = jongo.getCollection(WorkflowPerformanceEntity::class.simpleName)
    val workflowResults = workflowCollection.find()
            .`as`(WorkflowPerformanceEntity::class.java)
            .use { cursor ->
        cursor.associateBy(WorkflowPerformanceEntity::datasetName) {
            it.trainScore to it.testScore
        }
    }

    singleClassifierReport(jongo, workflowResults)
    tpotBaselineReport(jongo, workflowResults)
}

private fun tpotBaselineReport(jongo: Jongo, workflowResults: Map<String, Pair<Double, Double>>) {
    val tpotCollection = jongo.getCollection(TpotPerformanceEntity::class.simpleName)
    val tpotResults = tpotCollection.find()
            .`as`(TpotPerformanceEntity::class.java)
            .use { cursor ->
                cursor.associateBy(TpotPerformanceEntity::datasetName) {
                    it.trainScore to it.testScore
                }
            }
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet()
    val firstRow = sheet.createRow(0)
    firstRow.createCell(0).setCellValue("Dataset Name")
    firstRow.createCell(1).setCellValue("TPOT (Train)")
    firstRow.createCell(2).setCellValue("TPOT (Test)")
    firstRow.createCell(3).setCellValue("Workflow (Train)")
    firstRow.createCell(4).setCellValue("Workflow (Test)")

    val font = workbook.createFont()
    font.bold = true

    var rowIndex = 1
    for (datasetName in DATASETS) {
        if (datasetName in workflowResults && datasetName in tpotResults) {
            val (train, test) = workflowResults[datasetName]!!
            val (tpotTrain, tpotTest) = tpotResults[datasetName]!!

            val max = maxOf(test, tpotTest)
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(datasetName)
            row.createCell(1, tpotTrain, Double.MAX_VALUE, font)
            row.createCell(2, tpotTest, max, font)
            row.createCell(3, train, Double.MAX_VALUE, font)
            row.createCell(4, test, max, font)
        }
    }

    FileOutputStream("tpot-baseline.xlsx")
            .buffered()
            .use(workbook::write)
}

private fun singleClassifierReport(jongo: Jongo, workflowResults: Map<String, Pair<Double, Double>>) {
    val collection = jongo.getCollection(SingleClassifierTuningEntity::class.simpleName)
    val rfResults = collection.loadResults("RF")
    val svmResults = collection.loadResults("SVM")

    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet()
    val firstRow = sheet.createRow(0)
    firstRow.createCell(0).setCellValue("Dataset Name")
    firstRow.createCell(1).setCellValue("RF")
    firstRow.createCell(2).setCellValue("SVM")
    firstRow.createCell(3).setCellValue("Workflow (Train)")
    firstRow.createCell(4).setCellValue("Workflow (Test)")

    val font = workbook.createFont()
    font.bold = true

    var rowIndex = 1
    for (datasetName in DATASETS) {
        workflowResults[datasetName]?.also { (train, test) ->
            val rfScore = rfResults[datasetName]!!
            val svmScore = svmResults[datasetName]!!
            val max = maxOf(rfScore, svmScore, maxOf(train, test))

            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(datasetName)
            row.createCell(1, rfScore, max, font)
            row.createCell(2, svmScore, max, font)
            row.createCell(3, train, max, font)
            row.createCell(4, test, max, font)
        }
    }

    FileOutputStream("single-classifier-baseline.xlsx")
            .buffered()
            .use(workbook::write)
}

private fun XSSFRow.createCell(columnIndex: Int, value: Double, max: Double, font: XSSFFont) {
    val cell = createCell(columnIndex)
    val stringValue = value.round(4).toString()
    if (value == max) {
        val textString = XSSFRichTextString(stringValue)
        textString.applyFont(font)
        cell.setCellValue(textString)
    } else {
        cell.setCellValue(stringValue)
    }
}

private fun Double.round(precision: Int): Double {
    val shift = Math.pow(10.0, precision.toDouble())
    return (this * shift).toInt() / shift
}

private fun MongoCollection.loadResults(classifierName: String): Map<String, Double> {
    val cursor = find("{classifier_name: \"$classifierName\"}")
            .`as`(SingleClassifierTuningEntity::class.java)
    return cursor.use { it.associateBy(SingleClassifierTuningEntity::datasetName,
            SingleClassifierTuningEntity::score) }
}

private fun createJongo(dbName: String): Jongo {
    val db = MongoClient().getDB(dbName)
    val mapper = JacksonMapper.Builder()
            .registerModule(KotlinModule())
            .build()
    return Jongo(db, mapper)
}
