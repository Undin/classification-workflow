package com.warrior.classification_workflow.baseline

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.Session
import org.hibernate.SessionFactory
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

        override fun save(result: ResultEntity) {
            synchronized(generator) {
                generator.writeObject(result)
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
        private val session: Session

        init {
            sessionFactory = Configuration()
                    .configure()
                    .buildSessionFactory()
            session = sessionFactory.openSession()
        }

        override fun save(result: ResultEntity) {
            session.saveInTransaction(result)
        }

        override fun close() {
            session.close()
            sessionFactory.close()
        }
    }

    abstract fun save(result: ResultEntity)
}
