package org.ktapi.error

import io.sentry.EventProcessor
import io.sentry.Hint
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.protocol.Request
import io.sentry.protocol.User
import mu.KotlinLogging
import org.ktapi.Environment
import org.ktapi.config
import org.ktapi.configHasKey
import org.ktapi.configList
import javax.servlet.http.HttpServletRequest

/**
 * ErrorReporter implementation for Sentry.
 *
 * - sentry.dsn config property is used to set the DSN for Sentry
 * - sentry.packages config property is used to tell Sentry what package to include in error reporting
 */
object SentryErrorReporter : ErrorReporter {
    private val logger = KotlinLogging.logger {}
    private var initialized = false
    private const val sentryDsnKey = "sentry.dsn"
    private const val sentryPackagesKey = "sentry.packages"

    fun init() {
        when {
            Environment.isLocal -> logger.warn("Not using Sentry locally")
            !configHasKey(sentryDsnKey) -> logger.warn("No Sentry DSN found in config")
            else -> {
                val packages = configList<String>(sentryPackagesKey, listOf())
                if (Environment.version != null) System.setProperty("sentry.release", Environment.version)
                Sentry.init { options ->
                    options.dsn = config(sentryDsnKey)
                    options.environment = Environment.name
                    options.release = Environment.version
                    options.inAppIncludes.addAll(packages)
                }
                initialized = true
            }
        }
    }

    override fun setHttpInfo(request: HttpServletRequest) {
        Sentry.configureScope { it.addEventProcessor(HttpEventProcessor(request)) }
    }

    override fun report(t: Throwable, additionalInfo: Map<String, Any>?) {
        if (initialized) {
            additionalInfo?.forEach { Sentry.setExtra(it.key, it.value.toString()) }
            Sentry.captureException(t)
        } else {
            logger.error(t) { additionalInfo }
        }
    }

    override fun report(message: String, additionalInfo: Map<String, Any>?) {
        if (initialized) {
            additionalInfo?.forEach { Sentry.setExtra(it.key, it.value.toString()) }
            Sentry.captureMessage(message)
        } else {
            logger.error("$message: $additionalInfo")
        }
    }


    override fun setUserInfo(userId: Long, email: String?, ipAddress: String?) {
        Sentry.setUser(User().apply {
            this.id = userId.toString()
            this.ipAddress = ipAddress
            this.email = email
        })
    }

    override fun addBreadcrumb(message: String) {
        Sentry.addBreadcrumb(message)
    }

    override fun clearBreadcrumbs() {
        Sentry.clearBreadcrumbs()
    }

    override fun addContext(key: String, value: Any) {
        Sentry.setExtra(key, value.toString())
    }

    override fun addContext(values: Map<String, Any>?) {
        values?.forEach { Sentry.setExtra(it.key, it.value.toString()) }
    }

    override fun removeContext(keys: Collection<String>?) {
        keys?.forEach { removeContext(it) }
    }

    private fun removeContext(key: String) = Sentry.removeExtra(key)

    override fun removeContext(vararg keys: String) {
        when (keys.size) {
            0 -> Unit
            1 -> removeContext(keys.first())
            else -> keys.forEach { removeContext(it) }
        }
    }

    class HttpEventProcessor(private val request: HttpServletRequest) : EventProcessor {
        private val headersToStrip = listOf("authorization", "cookie")

        override fun process(event: SentryEvent, hint: Hint): SentryEvent? {
            val sentryRequest = Request()
            sentryRequest.method = request.method
            sentryRequest.queryString = request.queryString
            sentryRequest.url = request.requestURL.toString()
            sentryRequest.headers = request.headerNames.toList()
                .filter { !headersToStrip.contains(it.lowercase()) }
                .associateWith { request.getHeader(it) }

            event.request = sentryRequest
            return event
        }
    }
}