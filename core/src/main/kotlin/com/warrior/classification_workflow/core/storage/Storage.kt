package com.warrior.classification_workflow.core.storage

import java.io.Closeable

/**
 * Created by warrior on 1/27/17.
 */
interface Storage : Closeable {
    fun save(any: Any)
    fun <T> get(clazz: Class<T>): List<T>
}
