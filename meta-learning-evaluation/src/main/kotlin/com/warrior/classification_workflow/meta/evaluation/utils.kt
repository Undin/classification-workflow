package com.warrior.classification_workflow.meta.evaluation

import kotlinx.support.jdk8.collections.parallelStream
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.util.Supplier

/**
 * Created by warrior on 11/28/16.
 */
inline fun <T> Logger.withLog(message: String, block: () -> T): T {
    info(Supplier { "start $message" })
    val result = block()
    info(Supplier { "end $message" })
    return result
}

fun Map<String, String>.toArray(): Array<String>
        = flatMap { listOf(it.key, it.value) }.toTypedArray()

fun <T> Collection<T>.forEachParallel(action: (T) -> Unit)
        = parallelStream().forEach { action(it) }
