package com.warrior.classification_workflow.reports

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mongodb.MongoClient
import com.warrior.classification_workflow.WorkflowPerformanceEntity
import com.warrior.classification_workflow.baseline.single.SingleClassifierPerformanceEntity
import com.warrior.classification_workflow.baseline.tpot.TpotPerformanceEntity
import com.warrior.classification_workflow.core.PerformanceEntity
import com.warrior.classification_workflow.stacking.WorkflowStackingPerformanceEntity
import org.apache.poi.xssf.usermodel.*
import org.jongo.Jongo
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

    val rfResult = jongo.loadPerformanceResult<SingleClassifierPerformanceEntity>("{classifier_name: \"RF\"}")
    val svmResult = jongo.loadPerformanceResult<SingleClassifierPerformanceEntity>("{classifier_name: \"SVM\"}")
    val tpotResults = jongo.loadPerformanceResult<TpotPerformanceEntity>()
    val workflowResults = jongo.loadPerformanceResult<WorkflowPerformanceEntity>()
    val workflowStackingResults = jongo.loadPerformanceResult<WorkflowStackingPerformanceEntity>()
    val resultList = listOf(rfResult, svmResult, tpotResults, workflowResults, workflowStackingResults)

    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet()
    val firstRow = sheet.createRow(0)
    firstRow.createCell(0).setCellValue("Dataset Name")
    firstRow.createCell(1).setCellValue("RF")
    firstRow.createCell(2).setCellValue("SVM")
    firstRow.createCell(3).setCellValue("Tpot")
    firstRow.createCell(4).setCellValue("Workflow")
    firstRow.createCell(5).setCellValue("Workflow stacking")

    val font = workbook.createFont()
    font.bold = true

    var rowIndex = 1
    for (datasetName in DATASETS) {
        val row = sheet.createRow(rowIndex++)
        row.createCell(0).setCellValue(datasetName)

        val datasetResults = resultList.map { it.getOrDefault(datasetName, -1.0) }
        val bestResult = datasetResults.max()!!

        for ((i, result) in datasetResults.withIndex()) {
            if (result != -1.0) {
                row.createCell(i + 1, result, bestResult, font)
            } else {
                row.createCell(i + 1).setCellValue("-")
            }
        }
    }

    FileOutputStream("report.xlsx")
            .buffered()
            .use(workbook::write)
}

inline private fun <reified T : PerformanceEntity> Jongo.loadPerformanceResult(query: String = "{}"): Map<String, Double> {
    val collection = getCollection(T::class.java.simpleName)
    return collection.find(query).`as`(T::class.java).use { cursor ->
        cursor.associateBy(PerformanceEntity::name, PerformanceEntity::score)
    }
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

private fun createJongo(dbName: String): Jongo {
    val db = MongoClient().getDB(dbName)
    val mapper = JacksonMapper.Builder()
            .registerModule(KotlinModule())
            .build()
    return Jongo(db, mapper)
}
