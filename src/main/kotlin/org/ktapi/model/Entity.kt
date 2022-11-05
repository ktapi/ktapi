package org.ktapi.model

import org.ktorm.entity.Entity
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.jvmErasure

interface WithId {
    val id: Long
}

interface WithDates : WithId {
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
}

interface EntityWithId<E : Entity<E>> : Entity<E>, WithId

interface EntityWithDates<E : Entity<E>> : EntityWithId<E>, WithDates

fun <T : Entity<T>> T.populateFrom(data: Map<String, Any?>, dataClass: KClass<*>) = apply {
    dataClass.memberProperties.map {
        if (data.containsKey(it.name)) {
            val returnType = it.returnType.jvmErasure
            val value = data[it.name]
            this[it.name] = when {
                value is String && returnType.isSubclassOf(Enum::class) ->
                    returnType.staticFunctions.find { f -> f.name == "valueOf" }!!.call(value)

                else -> value
            }
        }
    }
}

fun <T : Entity<T>> T.populateFrom(data: Any) = this.apply {
    data::class.memberProperties.forEach {
        this[it.name] = it.getter.call(data)
    }
}

object EmptyValue

fun <T> Entity<*>.lazyLoad(name: String, loader: () -> T?): T? {
    val key = "lazy_$name"
    var value = this[key]

    if (value == null) {
        value = loader() ?: EmptyValue
        this[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    return if (value == EmptyValue) null else value as T?
}

fun <T : EntityWithId<T>> Entity<*>.lazyLoad(loader: EntityTable<T>, id: Long) =
    this.lazyLoad(loader::class.simpleName!! + id) { loader.findById(id) }

fun Entity<*>.clearLazyLoad(name: String) {
    this["lazy_$name"] = null
}

fun List<WithId>.ids() = this.map { it.id }

fun <T : Entity<T>, E : Entity<E>> preload(
    list: List<T>,
    name: String,
    sourceId: String,
    destId: String,
    lookup: (ids: List<Long>) -> List<E>
): List<T> {
    val data = lookup(list.map { it[sourceId] as Long }).groupBy { it[destId] }
    list.forEach { item ->
        item.lazyLoad(name) { data[item[sourceId]] }
    }
    return list
}
