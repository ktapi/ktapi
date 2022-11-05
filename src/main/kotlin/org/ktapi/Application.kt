package org.ktapi

import mu.KotlinLogging
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
 */
object Application {
    private val initStart = System.currentTimeMillis()
    val name: String
    val TraceLogger: TraceLogger

    init {
        Environment.init()

        name = config("application.name")

        TraceLogger = config("application.traceLogger", ConsoleLogger)
    }

    operator fun invoke(init: () -> Any? = {}) {
        val logger = KotlinLogging.logger {}

        init()
        logger.info("Application initialized in ${nowMillis() - initStart}ms")
    }
}
