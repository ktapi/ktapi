package org.ktapi.model

import org.ktapi.db.Mode
import org.ktapi.db.ReadWrite
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.*
import java.time.LocalDateTime
import kotlin.reflect.KClass

open class EntityTable<E : EntityWithId<E>>(
    tableName: String,
    alias: String? = null,
    entityClass: KClass<E>? = null
) : Table<E>(tableName = tableName, alias = alias, entityClass = entityClass) {
    val id = long("id").primaryKey().bindTo { it.id }

    fun E.create() = apply { addEntity(this) }

    fun findById(id: Long?, mode: Mode = ReadWrite) = if (id == null || id < 1) null else findOne(mode) { it.id eq id }
    fun findByIds(ids: Collection<Long>, mode: Mode = ReadWrite) =
        if (ids.isEmpty()) listOf() else findList(mode) { it.id inList ids }
}

open class EntityWithDatesTable<E : EntityWithDates<E>>(
    tableName: String,
    alias: String? = null,
    entityClass: KClass<E>? = null
) : EntityTable<E>(tableName, alias, entityClass) {
    val createdAt = datetime("created_at").bindTo { it.createdAt }
    val updatedAt = datetime("updated_at").bindTo { it.updatedAt }

    fun setCreatedAt(id: Long, time: LocalDateTime): E {
        update {
            set(it.createdAt, time)
            where {
                it.id eq id
            }
        }
        return findById(id)!!
    }

    fun setUpdatedAt(id: Long, time: LocalDateTime): E {
        update {
            set(it.updatedAt, time)
            where {
                it.id eq id
            }
        }
        return findById(id)!!
    }

    fun refreshUpdatedAt(id: Long) {
        setUpdatedAt(id, LocalDateTime.now())
    }
}

fun <T : Entity<T>, E : EntityWithId<E>> preload(list: List<T>, loader: EntityTable<E>, idProperty: String): List<T> {
    val items = loader.findByIds(list.map { it[idProperty] as Long })
    list.forEach {
        val id = it[idProperty] as Long
        it.lazyLoad(loader::class.simpleName!! + id) { items.find { item -> item.id == id } }
    }

    return list
}

fun <E : Entity<E>> Table<E>.addEntity(entity: E): Int {
    return ReadWrite.db.sequenceOf(this).add(entity)
}

fun <E : Entity<E>> Table<E>.updateEntity(entity: E): Int {
    return ReadWrite.db.sequenceOf(this).update(entity)
}

inline fun <E : Any, T : BaseTable<E>> T.findOne(
    mode: Mode = ReadWrite,
    predicate: (T) -> ColumnDeclaring<Boolean>
): E? {
    return mode.db.sequenceOf(this).find(predicate)
}

fun <E : Any> BaseTable<E>.findAll(mode: Mode = ReadWrite): List<E> {
    return mode.db.sequenceOf(this).toList()
}

fun BaseTable<*>.crossJoin(right: BaseTable<*>, on: ColumnDeclaring<Boolean>? = null): QuerySource {
    return ReadWrite.db.from(this).crossJoin(right, on)
}

fun BaseTable<*>.innerJoin(right: BaseTable<*>, on: ColumnDeclaring<Boolean>? = null): QuerySource {
    return ReadWrite.db.from(this).innerJoin(right, on)
}

fun BaseTable<*>.leftJoin(right: BaseTable<*>, on: ColumnDeclaring<Boolean>? = null): QuerySource {
    return ReadWrite.db.from(this).leftJoin(right, on)
}

fun BaseTable<*>.rightJoin(right: BaseTable<*>, on: ColumnDeclaring<Boolean>? = null): QuerySource {
    return ReadWrite.db.from(this).rightJoin(right, on)
}

fun BaseTable<*>.joinReferencesAndSelect(mode: Mode = ReadWrite): Query {
    return mode.db.from(this).joinReferencesAndSelect()
}

fun BaseTable<*>.select(columns: Collection<ColumnDeclaring<*>>, mode: Mode = ReadWrite): Query {
    return mode.db.from(this).select(columns)
}

fun BaseTable<*>.select(vararg columns: ColumnDeclaring<*>) = select(columns.asList())
fun BaseTable<*>.select(mode: Mode = ReadWrite, vararg columns: ColumnDeclaring<*>) = select(columns.asList(), mode)

fun BaseTable<*>.selectDistinct(columns: Collection<ColumnDeclaring<*>>, mode: Mode = ReadWrite): Query {
    return mode.db.from(this).selectDistinct(columns)
}

fun BaseTable<*>.selectDistinct(vararg columns: ColumnDeclaring<*>) = selectDistinct(columns.asList())
fun BaseTable<*>.selectDistinct(mode: Mode = ReadWrite, vararg columns: ColumnDeclaring<*>) =
    selectDistinct(columns.asList(), mode)

inline fun <E : Any, T : BaseTable<E>> T.findList(
    mode: Mode = ReadWrite,
    predicate: (T) -> ColumnDeclaring<Boolean>
): List<E> {
    return mode.db.sequenceOf(this).filter(predicate).toList()
}

fun <T : BaseTable<*>> T.update(block: UpdateStatementBuilder.(T) -> Unit): Int {
    return ReadWrite.db.update(this, block)
}

fun <T : BaseTable<*>> T.batchUpdate(block: BatchUpdateStatementBuilder<T>.() -> Unit): IntArray {
    return ReadWrite.db.batchUpdate(this, block)
}

fun <T : BaseTable<*>> T.insert(block: AssignmentsBuilder.(T) -> Unit): Int {
    return ReadWrite.db.insert(this, block)
}

fun <T : BaseTable<*>> T.batchInsert(block: BatchInsertStatementBuilder<T>.() -> Unit): IntArray {
    return ReadWrite.db.batchInsert(this, block)
}

fun <T : BaseTable<*>> T.insertAndGenerateKey(block: AssignmentsBuilder.(T) -> Unit): Any {
    return ReadWrite.db.insertAndGenerateKey(this, block)
}

fun <T : BaseTable<*>> T.delete(predicate: (T) -> ColumnDeclaring<Boolean>): Int {
    return ReadWrite.db.delete(this, predicate)
}

fun BaseTable<*>.deleteAll(): Int {
    return ReadWrite.db.deleteAll(this)
}

inline fun <E : Any, T : BaseTable<E>> T.all(
    mode: Mode = ReadWrite,
    predicate: (T) -> ColumnDeclaring<Boolean>
): Boolean {
    return mode.db.sequenceOf(this).all(predicate)
}

fun <E : Any, T : BaseTable<E>> T.any(mode: Mode = ReadWrite): Boolean {
    return mode.db.sequenceOf(this).any()
}

inline fun <E : Any, T : BaseTable<E>> T.any(
    mode: Mode = ReadWrite,
    predicate: (T) -> ColumnDeclaring<Boolean>
): Boolean {
    return mode.db.sequenceOf(this).any(predicate)
}

fun <E : Any, T : BaseTable<E>> T.none(mode: Mode = ReadWrite): Boolean {
    return mode.db.sequenceOf(this).none()
}

inline fun <E : Any, T : BaseTable<E>> T.none(
    mode: Mode = ReadWrite,
    predicate: (T) -> ColumnDeclaring<Boolean>
): Boolean {
    return mode.db.sequenceOf(this).none(predicate)
}

fun <E : Any, T : BaseTable<E>> T.isEmpty(mode: Mode = ReadWrite): Boolean {
    return mode.db.sequenceOf(this).isEmpty()
}

fun <E : Any, T : BaseTable<E>> T.isNotEmpty(mode: Mode = ReadWrite): Boolean {
    return mode.db.sequenceOf(this).isNotEmpty()
}

fun <E : Any, T : BaseTable<E>> T.count(mode: Mode = ReadWrite): Int {
    return mode.db.sequenceOf(this).count()
}

inline fun <E : Any, T : BaseTable<E>> T.count(
    mode: Mode = ReadWrite,
    predicate: (T) -> ColumnDeclaring<Boolean>
): Int {
    return mode.db.sequenceOf(this).count(predicate)
}

inline fun <E : Any, T : BaseTable<E>, C : Number> T.sumBy(
    mode: Mode = ReadWrite,
    selector: (T) -> ColumnDeclaring<C>
): C? {
    return mode.db.sequenceOf(this).sumBy(selector)
}

inline fun <E : Any, T : BaseTable<E>, C : Comparable<C>> T.maxBy(
    mode: Mode = ReadWrite,
    selector: (T) -> ColumnDeclaring<C>
): C? {
    return mode.db.sequenceOf(this).maxBy(selector)
}

inline fun <E : Any, T : BaseTable<E>, C : Comparable<C>> T.minBy(
    mode: Mode = ReadWrite,
    selector: (T) -> ColumnDeclaring<C>
): C? {
    return mode.db.sequenceOf(this).minBy(selector)
}

inline fun <E : Any, T : BaseTable<E>> T.averageBy(
    mode: Mode = ReadWrite,
    selector: (T) -> ColumnDeclaring<out Number>
): Double? {
    return mode.db.sequenceOf(this).averageBy(selector)
}