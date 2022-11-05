package org.ktapi.trace

import mu.KotlinLogging
import java.time.LocalDateTime

object ConsoleLogger : TraceLogger {
    private val logger = KotlinLogging.logger("TraceLogger")

    override fun log(trace: TraceData) {
        logger.warn(trace.toString())
    }

    override fun loadTraceData(
        dateRange: ClosedRange<LocalDateTime>?,
        type: String?,
        name: String?,
        durationMin: Int?,
        dbTimeMin: Int?
    ) = listOf<TraceData>()
}