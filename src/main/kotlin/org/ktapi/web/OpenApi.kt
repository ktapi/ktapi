package org.ktapi.web

import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.DocumentedHandler
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import org.ktapi.model.ValidationError

fun OpenApiDocumentation.jsonValidationErrors() = jsonArray<ValidationError>("400")

fun OpenApiDocumentation.idPathParam(name: String = "id") = pathParam<Long>(name)

class DocumentedHandlerBuilder {
    private lateinit var handler: (ctx: Context) -> Unit
    private val document = document()

    fun handler(block: (ctx: Context) -> Unit) {
        handler = block
    }

    fun doc(operationId: String, summary: String? = null, tag: String? = null, block: OpenApiDocumentation.() -> Unit) {
        document.operation {
            it.operationId = operationId
            if (tag != null) it.addTagsItem(tag)
            if (summary != null) it.summary = summary
        }
        block.invoke(document)
    }

    fun doc(
        operationId: String,
        summary: String? = null,
        tags: List<String> = listOf(),
        block: OpenApiDocumentation.() -> Unit
    ) {
        document.operation {
            it.operationId = operationId
            it.tags = tags
            if (summary != null) it.summary = summary
        }
        block.invoke(document)
    }

    fun toDocumented() = documented(document, handler)
}

fun documentedHandler(block: DocumentedHandlerBuilder.() -> Unit): DocumentedHandler {
    val builder = DocumentedHandlerBuilder()
    block.invoke(builder)
    return builder.toDocumented()
}