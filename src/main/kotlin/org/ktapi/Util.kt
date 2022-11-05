package org.ktapi

import java.time.*
import java.time.temporal.ChronoField
import kotlin.math.round

internal object Util {
    val classLoader: ClassLoader = Util.javaClass.classLoader
}

/**
 * This is a convenience class that adds an init function that can be called to
 * force the init block of the class to execute.
 *
 * @param blocks list of blocks of code that will be executed sequentially as part of the init
 */
open class Init(vararg blocks: () -> Any?) {
    init {
        blocks.forEach { it() }
    }

    /**
     * Ensures the init block on the object is run
     */
    fun init() = Unit
}

// String Functions
/**
 * @return the contents of the resource path in the string or null if it doesn't exist
 */
fun String.resourceAsString() = Util.classLoader.getResource(this)?.readText()

/**
 * @return true if a classpath resource exists with the path in the string
 */
fun String.resourceExists() = Util.classLoader.getResource(this) != null

// Date Functions
fun now() = LocalDateTime.now()
fun today() = LocalDate.now()
fun nowMillis() = System.currentTimeMillis()

fun Int.minutesAgo(): LocalDateTime = LocalDateTime.now().minusMinutes(this.toLong())
fun Int.minutesFromNow(): LocalDateTime = LocalDateTime.now().plusMinutes(this.toLong())
fun Int.hoursAgo(): LocalDateTime = LocalDateTime.now().minusHours(this.toLong())
fun Int.hoursFromNow(): LocalDateTime = LocalDateTime.now().plusHours(this.toLong())
fun Int.daysAgo(): LocalDateTime = LocalDateTime.now().minusDays(this.toLong())
fun Int.daysFromNow(): LocalDateTime = LocalDateTime.now().plusDays(this.toLong())

fun Int.secondsInMillis() = this * 1000L
fun Int.minutesInMillis() = this * 60 * 1000L
fun Int.hoursInMillis() = this * 60 * 60 * 1000L

fun Long.toLocalDateTimeFromMillis(zone: ZoneId = ZoneOffset.UTC): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zone)

fun LocalDateTime.millisSince(other: LocalDateTime) = this.toMillisUtc() - other.toMillisUtc()
fun LocalDateTime.secondsSince(other: LocalDateTime) = round(this.millisSince(other) / 1000.0).toLong()
fun LocalDateTime.millisUntil(other: LocalDateTime) = -this.millisSince(other)
fun LocalDateTime.secondsUntil(other: LocalDateTime) = -this.secondsSince(other)
fun LocalDateTime.toMillisUtc() = this.toEpochSecond(ZoneOffset.UTC) * 1000 + this.get(ChronoField.MILLI_OF_SECOND)

fun LocalDateTime.toStartOfMinute(): LocalDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute)
fun LocalDateTime.toStartOfHour(): LocalDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, 0)
fun LocalDateTime.toStartOfDay(): LocalDateTime = LocalDateTime.of(year, month, dayOfMonth, 0, 0)
fun LocalDateTime.toStartOfMonth(): LocalDateTime = LocalDateTime.of(year, month, 1, 0, 0)