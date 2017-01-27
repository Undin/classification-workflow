package com.warrior.classification_workflow.core.storage.mongo

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mongodb.MongoClient
import com.warrior.classification_workflow.core.storage.Storage
import org.jongo.Jongo
import org.jongo.marshall.jackson.JacksonMapper

/**
 * Created by warrior on 1/27/17.
 */
class MongoStorage : Storage {

    private val jongo: Jongo

    init {
        val db = MongoClient().getDB("master")
        val mapper = JacksonMapper.Builder()
                .registerModule(KotlinModule())
                .build()
        jongo = Jongo(db, mapper)
    }

    override fun save(any: Any) {
        val collection = jongo.getCollection(any.javaClass.simpleName)
        collection.save(any)
    }

    override fun <T> get(clazz: Class<T>): List<T> {
        val collection = jongo.getCollection(clazz.simpleName)
        return collection.find().`as`(clazz).toList()
    }

    override fun close() {}
}
