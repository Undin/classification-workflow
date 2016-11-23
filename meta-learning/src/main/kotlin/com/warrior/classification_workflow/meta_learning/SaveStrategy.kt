package com.warrior.classification_workflow.meta_learning

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import org.hibernate.cfg.Configuration
import java.io.Closeable
import java.io.File

/**
 * Created by warrior on 19/09/16.
 */
sealed class SaveStrategy : Closeable {

    class JsonSaveStrategy(path: String) : SaveStrategy() {

        private val mapper = ObjectMapper()
        private val generator: JsonGenerator

        init {
            mapper.disable(MapperFeature.AUTO_DETECT_FIELDS,
                    MapperFeature.AUTO_DETECT_GETTERS,
                    MapperFeature.AUTO_DETECT_IS_GETTERS)
            generator = mapper.factory.createGenerator(File(path), JsonEncoding.UTF8)
            generator.writeStartArray()
        }

        override fun save(any: Any) {
            synchronized(generator) {
                generator.writeObject(any)
                generator.flush()
            }
        }

        override fun close() {
            generator.writeEndArray()
            generator.close()
        }
    }

    class DatabaseSaveStrategy : SaveStrategy() {

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

    abstract fun save(any: Any)

    companion object {

        @JvmStatic
        fun fromString(saveStrategy: String, outFolder: String? = null): SaveStrategy {
            return when (saveStrategy) {
                "json" -> {
                    val folder = outFolder ?: throw IllegalAccessException("outFolder must be non null for json save strategy")
                    File(folder).mkdirs()
                    JsonSaveStrategy("$folder/result-${System.currentTimeMillis()}.json")
                }
                "db" -> DatabaseSaveStrategy()
                else -> throw IllegalArgumentException("unknown value for save strategy: $saveStrategy")
            }
        }
    }
}
