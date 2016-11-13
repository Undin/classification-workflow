package com.warrior.classification_workflow.baseline.dataset_info

import com.warrior.classification_workflow.baseline.saveInTransaction
import com.warrior.classification_workflow.core.load
import com.warrior.classification_workflow.core.load
import org.hibernate.cfg.Configuration
import weka.gui.ExtensionFileFilter
import java.io.File

/**
 * Created by warrior on 17/09/16.
 */
fun main(args: Array<String>) {
    val files = File("datasets").listFiles(ExtensionFileFilter("arff", ""))
    if (files != null) {
        val sessionFactory = Configuration()
                .configure()
                .buildSessionFactory()
        val session = sessionFactory.openSession()
        try {
            for (file in files) {
                val data = load(file.absolutePath)
                val info = DatasetInfo(file.nameWithoutExtension, data.numAttributes(), data.numInstances(), data.numClasses())
                session.saveInTransaction(info)
            }
        } finally {
            session.close()
            sessionFactory.close()
        }
    }
}