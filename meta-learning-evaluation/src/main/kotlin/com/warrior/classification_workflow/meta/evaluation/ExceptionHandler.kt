package com.warrior.classification_workflow.meta.evaluation

import org.apache.logging.log4j.LogManager

/**
 * Created by warrior on 11/29/16.
 */
class ExceptionHandler : Thread.UncaughtExceptionHandler {

    private val logger = LogManager.getFormatterLogger(ExceptionHandler::class.java)

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        if (e != null) {
            logger.error(e.message, e)
        }
    }
}
