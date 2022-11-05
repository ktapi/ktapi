package org.ktapi.model

import org.ktapi.model.Validation.field
import org.ktapi.model.Validation.notBlank
import org.ktapi.model.Validation.validate
import org.ktorm.entity.Entity
import org.ktorm.schema.boolean
import org.ktorm.schema.varchar
import java.util.*

interface SomethingData : WithDates {
    var name: String
    var value: String?
    var enabled: Boolean
}

interface Something : EntityWithDates<Something>, SomethingData {
    companion object : Entity.Factory<Something>()

    fun validate() = validate {
        field(this::name) { notBlank() }
    }

    val aLazyValue: String
        get() = lazyLoad("aLazyValue") { UUID.randomUUID().toString() }!!
}

object SomethingRepo : EntityWithDatesTable<Something>("something") {
    val name = varchar("name").bindTo { it.name }
    val value = varchar("value").bindTo { it.value }
    val enabled = boolean("enabled").bindTo { it.enabled }

    fun create(name: String) = Something {
        this.name = name
    }.create()
}