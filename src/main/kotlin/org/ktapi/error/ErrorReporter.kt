package org.ktapi.error

import javax.servlet.http.HttpServletRequest

/**
 * Interface that defines an error reporter.
 */
interface ErrorReporter {
    fun report(t: Throwable, additionalInfo: Map<String, Any>? = null)
    fun report(message: String, additionalInfo: Map<String, Any>? = null)
    fun setHttpInfo(request: HttpServletRequest)
    fun setUserInfo(userId: Long, email: String? = null, ipAddress: String? = null)
    fun addBreadcrumb(message: String)
    fun clearBreadcrumbs()
    fun addContext(key: String, value: Any)
    fun addContext(values: Map<String, Any>?)
    fun removeContext(keys: Collection<String>?)
    fun removeContext(vararg keys: String)
}