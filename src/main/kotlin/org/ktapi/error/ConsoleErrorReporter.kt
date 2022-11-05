package org.ktapi.error

import mu.KotlinLogging
import javax.servlet.http.HttpServletRequest

object ConsoleErrorReporter : ErrorReporter {
    private val logger = KotlinLogging.logger {}

    override fun report(t: Throwable, additionalInfo: Map<String, Any>?) {
        logger.error(t) { "Error" }
    }

    override fun report(message: String, additionalInfo: Map<String, Any>?) {
        logger.warn(message)
    }

    override fun setHttpInfo(request: HttpServletRequest) {
    }

    override fun setUserInfo(userId: Long, email: String?, ipAddress: String?) {
    }

    override fun addBreadcrumb(message: String) {
    }

    override fun clearBreadcrumbs() {
    }

    override fun addContext(key: String, value: Any) {
    }

    override fun addContext(values: Map<String, Any>?) {
    }

    override fun removeContext(keys: Collection<String>?) {
    }

    override fun removeContext(vararg keys: String) {
    }
}