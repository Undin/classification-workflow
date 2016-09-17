package com.warrior.classification.workflow.baseline

import org.hibernate.Session
import org.hibernate.Transaction

/**
 * Created by warrior on 17/09/16.
 */
internal fun <T> Session.saveInTransaction(t: T) {
    var transaction: Transaction? = null
    try {
        transaction = beginTransaction()
        save(t)
        transaction.commit()
    } catch (e: Exception) {
        e.printStackTrace()
        if (transaction != null) {
            transaction.rollback()
        }
    }
}
