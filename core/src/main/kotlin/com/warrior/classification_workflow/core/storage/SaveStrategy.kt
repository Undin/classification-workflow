package com.warrior.classification_workflow.core.storage

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.warrior.classification_workflow.core.storage.mongo.MongoStorage
import com.warrior.classification_workflow.core.storage.hibernate.HibernateStorage
import java.io.Closeable
import java.io.File

/**
 * Created by warrior on 19/09/16.
 */
sealed class SaveStrategy : Closeable {

    class JsonSaveStrategy(path: String) : SaveStrategy() {

        private val mapper = jacksonObjectMapper()
        private val generator: JsonGenerator

        init {
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

    class DatabaseSaveStrategy(private val storage: Storage) : SaveStrategy() {
        override fun save(any: Any) = storage.save(any)
        override fun close() = storage.close()
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
                "db" -> DatabaseSaveStrategy(HibernateStorage())
                "mongo" -> DatabaseSaveStrategy(MongoStorage())
                else -> throw IllegalArgumentException("unknown value for save strategy: $saveStrategy")
            }
        }
    }
}
