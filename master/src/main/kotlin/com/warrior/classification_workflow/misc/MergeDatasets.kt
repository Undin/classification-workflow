package com.warrior.classification_workflow.misc

import com.warrior.classification_workflow.core.save
import weka.core.Attribute
import weka.core.Instances
import weka.core.converters.CSVSaver
import weka.core.converters.ConverterUtils
import java.io.File

/**
 * Created by warrior on 2/28/17.
 */
fun main(args: Array<String>) {
    if (args.size != 2) {
        System.err.println("Illegal args.\nUsage: java -jar in_dir out_dir")
        System.exit(1)
    }
    val (inFolder, outFolder) = args
    File(outFolder).mkdirs()

    val datasets = listOf(
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

    for (dataset in datasets) {
        println(dataset)
        try {
            val train = ConverterUtils.DataSource.read("$inFolder/$dataset-train.csv")
            val test = ConverterUtils.DataSource.read("$inFolder/$dataset-test.csv")

            val attribute = Attribute("set_name", listOf("train", "test"))
            train.insertAttribute(attribute, 0)
            test.insertAttribute(attribute, 1)

            for (instance in test) {
                train += instance
            }

            save(train, "$outFolder/$dataset.csv", CSVSaver())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}

private fun Instances.insertAttribute(attribute: Attribute, valueIndex: Int) {
    insertAttributeAt(attribute, 0)
    for (instance in this) {
        instance.setValue(0, attribute.value(valueIndex))
    }
}