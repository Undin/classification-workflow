package com.warrior.classification.workflow.baseline

import com.warrior.classification.workflow.core.load
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.cfg.Configuration
import weka.classifiers.Classifier
import weka.classifiers.evaluation.Evaluation
import weka.classifiers.functions.LibSVM
import weka.classifiers.trees.J48
import weka.classifiers.trees.RandomForest
import weka.core.Instances
import weka.gui.ExtensionFileFilter
import java.io.File
import java.util.*

/**
 * Created by warrior on 17/07/16.
 */
fun main(args: Array<String>) {
    // disable libSVM logs
    libsvm.svm.svm_set_print_string_function { it ->  }

    val classifiers: List<Classifier> = listOf(RandomForest(), J48(), LibSVM())
    val files = File("datasets").listFiles(ExtensionFileFilter("arff", ""))

    if (files != null) {
        val sessionFactory = Configuration()
                .configure()
                .buildSessionFactory()
        val session = sessionFactory.openSession()

        try {
            classifiers.forEach { algo ->
                Arrays.stream(files)
                        .parallel()
                        .forEach { file ->
                            val data = load(file.absolutePath)
                            println("start ${file.nameWithoutExtension}")
                            val result = measure(algo, data, file.nameWithoutExtension)
                            save(result, session)
                            println("end ${file.nameWithoutExtension}")
                        }
            }
        } finally {
            session.close()
            sessionFactory.close()
        }
    }
}

private fun save(result: ResultEntity, session: Session) {
    var transaction: Transaction? = null
    try {
        transaction = session.beginTransaction()
        session.save(result)
        transaction.commit()
    } catch (e: Exception) {
        if (transaction != null) {
            transaction.rollback()
        }
    }
}

private fun measure(algo: Classifier, data: Instances, name: String): ResultEntity {
    val eval = Evaluation(data)
    eval.crossValidateModel(algo, data, 10, Random())
    return ResultEntity(name, algo.javaClass.name, "f-measure", eval.unweightedMacroFmeasure())
}
