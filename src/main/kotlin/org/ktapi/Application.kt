package org.ktapi

import mu.KotlinLogging
import org.ktapi.error.ConsoleErrorReporter
import org.ktapi.error.ErrorReporter
import org.ktapi.files.FileStorage
import org.ktapi.trace.ConsoleLogger
import org.ktapi.trace.TraceLogger

/**
 * This is the entry point for an application.
 *
 * Usually it's called from the main function like this:
 * ```
 * fun main() = Application {
 *   // Additional app initialization done here
 * }
 * ```
 *
 * These configuration properties can be set:
 * - application.name the name of the application
 * - application.traceLogger the TraceLogger to use for logging application traces, the default is ConsoleLogger
 * - application.fileStorage the FileStorage implementation to use for this application, by default this will not be initialized
 * - application.errorReporter the ErrorReporter to use for reporting errors, the default is ConsoleErrorReporter
 */
object Application {
    private val initStart = System.currentTimeMillis()
    val name: String
    val TraceLogger: TraceLogger
    val ErrorReporter: ErrorReporter
    lateinit var FileStorage: FileStorage

    init {
        Environment.init()

        name = config("application.name")

        TraceLogger = config("application.traceLogger", ConsoleLogger)
        ErrorReporter = config("application.errorReporter", ConsoleErrorReporter)

        val fileStorage: FileStorage? = configOrNull("application.fileStorage")
        if (fileStorage != null) FileStorage = fileStorage
    }

    operator fun invoke(init: () -> Any? = {}) {
        val logger = KotlinLogging.logger {}

        init()
        logger.info("Application initialized in ${nowMillis() - initStart}ms")
    }
}
