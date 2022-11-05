package org.ktapi.web

import io.javalin.http.Context
import org.ktapi.fromJson
import java.time.LocalDate

interface WebTraceExtraBuilder {
    fun build(context: Context): Map<String, Any?>?
}

object EmptyWebTraceExtraBuilder : WebTraceExtraBuilder {
    override fun build(context: Context): Map<String, Any?>? = null
}

val Context.idPathParam: Long
    get() = idPathParam()

fun Context.idPathParam(name: String = "id") = pathParam(name).toLong()

fun Context.intQueryParam(name: String) = queryParam(name)?.toInt()

fun Context.longQueryParam(name: String) = queryParam(name)?.toLong()

fun Context.dateQueryParam(name: String) = queryParam(name)?.let { LocalDate.parse(it) }

fun Context.jsonOr404(any: Any?) = if (any == null) status(404) else json(any)

inline fun <reified T> Context.bodyFromJson() = bodyAsBytes().fromJson<T>()