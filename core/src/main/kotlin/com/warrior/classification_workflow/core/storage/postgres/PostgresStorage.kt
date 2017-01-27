package com.warrior.classification_workflow.core.storage.postgres

import com.warrior.classification_workflow.core.storage.Storage
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import org.hibernate.cfg.Configuration

/**
 * Created by warrior on 1/27/17.
 */
class PostgresStorage : Storage {

    private val sessionFactory: SessionFactory
    private val session: ThreadLocal<Session>

    init {
        sessionFactory = Configuration()
                .configure()
                .buildSessionFactory()
        session = ThreadLocal.withInitial { sessionFactory.openSession() }
    }

    override fun save(any: Any) {
        session.get().saveInTransaction(any)
    }

    override fun close() {
        sessionFactory.close()
    }

    override fun <T> get(clazz: Class<T>): List<T> {
        val query = session.get().createQuery("from ${clazz.name}")
        return query.list() as List<T>
    }

    private fun Session.saveInTransaction(any: Any) {
        var transaction: Transaction? = null
        try {
            transaction = beginTransaction()
            save(any)
            transaction.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            if (transaction != null) {
                transaction.rollback()
            }
        }
    }
}
