package org.ktapi

import mu.KotlinLogging
import org.ktapi.cache.Cache
import org.ktapi.email.Email
import org.ktapi.error.ConsoleErrorReporter
import org.ktapi.error.ErrorReporter
import org.ktapi.files.FileStorage
import org.ktapi.queue.Queue
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
 * - application.errorReporter the ErrorReporter to use for reporting errors, the default is ConsoleErrorReporter
 * - application.traceLogger the TraceLogger to use for logging application traces, the default is ConsoleLogger
 * - application.logs the Logs implementation to use for this application, the default is LocalLogs
 * - application.cache the Cache implementation to use for this application, by default this will not be initialized
 * - application.queue the Queue implementation to use for this application, by default this will not be initialized
 * - application.email the Email implementation to use for this application, by default this will not be initialized
 * - application.fileStorage the FileStorage implementation to use for this application, by default this will not be initialized
 */
object Application {
    private val initStart = System.currentTimeMillis()
    val name: String
    val ErrorReporter: ErrorReporter
    val TraceLogger: TraceLogger
    lateinit var Cache: Cache
    lateinit var Queue: Queue
    lateinit var Email: Email
    lateinit var FileStorage: FileStorage

    init {
        Environment.init()

        name = config("application.name")

        ErrorReporter = config("application.errorReporter", ConsoleErrorReporter)
        TraceLogger = config("application.traceLogger", ConsoleLogger)

        val cache: Cache? = configOrNull("application.cache")
        if (cache != null) Cache = cache

        val queue: Queue? = configOrNull("application.queue")
        if (queue != null) Queue = queue

        val email: Email? = configOrNull("application.email")
        if (email != null) Email = email

        val fileStorage: FileStorage? = configOrNull("application.fileStorage")
        if (fileStorage != null) FileStorage = fileStorage
    }

    operator fun invoke(init: () -> Any? = {}) {
        val logger = KotlinLogging.logger {}

        init()
        logger.info("Application initialized in ${nowMillis() - initStart}ms")
    }
}
