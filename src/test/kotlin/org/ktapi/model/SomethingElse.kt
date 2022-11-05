package org.ktapi.model

import org.ktapi.model.Validation.field
import org.ktapi.model.Validation.notBlank
import org.ktapi.model.Validation.notNull
import org.ktapi.model.Validation.validate
import org.ktorm.entity.Entity
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface SomethingElseData : WithDates {
    var somethingId: Long
    var name: String
}

interface SomethingElse : EntityWithDates<SomethingElse>, SomethingElseData {
    companion object : Entity.Factory<SomethingElse>()

    fun validate() = validate {
        field(this::name) { notBlank() }
        field(this::somethingId) { notNull() }
    }

    val something: Something
        get() = lazyLoad(SomethingRepo, this.somethingId)!!
}

object SomethingElseRepo : EntityWithDatesTable<SomethingElse>("something_else") {
    val name = varchar("name").bindTo { it.name }
    val somethingId = long("something_id").bindTo { it.somethingId }
}