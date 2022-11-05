package org.ktapi.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.metrics.IMetricsTracker
import com.zaxxer.hikari.metrics.MetricsTrackerFactory
import mu.KotlinLogging
import org.ktapi.*
import org.ktapi.trace.Trace
import org.ktorm.database.Transaction
import org.ktorm.database.TransactionIsolation
import org.ktorm.schema.*
import org.ktorm.support.mysql.MySqlDialect
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.sql.DataSource
import kotlin.reflect.KClass

typealias KtormDatabse = org.ktorm.database.Database

/**
 * Represent ReadWrite or Read mode of DB. Read is used for main DB instance and ReadOnly is for read replicas.
 */
abstract class Mode {
    abstract val db: KtormDatabse
}

/**
 * Read only mode used for one or more read replicas
 */
object Read : Mode() {
    private var i = 0

    override val db: KtormDatabse
        get() = Database.reads[index]

    private val index: Int
        get() =
            if (Database.reads.size == 1) 0
            else synchronized(this) {
                val v = i++
                if (i == Database.reads.size) i = 0
                v
            }
}

/**
 * Read write mode used for main DB instance.
 */
object ReadWrite : Mode() {
    override val db: KtormDatabse = Database.readWrite
}

/**
 * This object initializes the DB connection and
 */
object Database : Init() {
    private val logger = KotlinLogging.logger {}
    val readWrite: KtormDatabse
    val reads: List<KtormDatabse>
    val dataSource: DataSource

    interface ParamOrResult
    data class Result(val name: String, val type: Type) : ParamOrResult
    data class Param(val value: Any?, val type: Type) : ParamOrResult

    fun result(name: String, type: Type) = Result(name, type)
    fun <T : Any> param(value: T?, type: Type) = Param(value, type)
    fun param(value: Any) = Param(value, value::class.toType())
    inline fun <reified T : Any> paramN(value: T?) = param(value, T::class.toType())

    enum class Type {
        Boolean, LocalDate, LocalTime, LocalDateTime, Float, Double, Short, Int, Long, String
    }

    fun KClass<*>.toType(): Type =
        when (this) {
            Boolean::class -> Type.Boolean
            LocalDate::class -> Type.LocalDate
            LocalTime::class -> Type.LocalTime
            LocalDateTime::class -> Type.LocalDateTime
            Float::class -> Type.Float
            Double::class -> Type.Double
            Short::class -> Type.Short
            Int::class -> Type.Int
            Long::class -> Type.Long
            String::class -> Type.String
            else -> throw Exception("Cannot determine Sql.Type for type: $this")
        }

    private fun Type.toSqlType(): SqlType<*> =
        when (this) {
            Type.Boolean -> BooleanSqlType
            Type.LocalDate -> DateSqlType
            Type.LocalTime -> TimeSqlType
            Type.LocalDateTime -> TimestampSqlType
            Type.Float -> FloatSqlType
            Type.Double -> DoubleSqlType
            Type.Short -> ShortSqlType
            Type.Int -> IntSqlType
            Type.Long -> LongSqlType
            Type.String -> VarcharSqlType
        }

    class Row(private val data: Map<String, Any?>) {
        fun boolean(column: String) = data[column] as Boolean
        fun booleanOrNull(column: String) = data[column] as Boolean?
        fun short(column: String) = data[column] as Short
        fun shortOrNull(column: String) = data[column] as Short?
        fun int(column: String) = data[column] as Int
        fun intOrNull(column: String) = data[column] as Int?
        fun long(column: String) = data[column] as Long
        fun longOrNull(column: String) = data[column] as Long?
        fun string(column: String) = data[column] as String
        fun stringOrNull(column: String) = data[column] as String?
        fun float(column: String) = data[column] as Float
        fun floatOrNull(column: String) = data[column] as Float?
        fun double(column: String) = data[column] as Double
        fun doubleOrNull(column: String) = data[column] as Double?
        fun localDateTime(column: String) = data[column] as LocalDateTime
        fun localDateTimeOrNull(column: String) = data[column] as LocalDateTime?
        fun localDate(column: String) = data[column] as LocalDate
        fun localDateOrNull(column: String) = data[column] as LocalDate?
        fun localTime(column: String) = data[column] as LocalTime
        fun localTimeOrNull(column: String) = data[column] as LocalTime?
        fun any(column: String) = data[column]
        inline fun <reified T> get(column: String) = any(column) as T
    }

    init {
        val driver = config<String>("db.driver")

        val createDataSource = { url: String ->
            HikariConfig().run {
                jdbcUrl = url
                driverClassName = driver
                username = config("db.username")
                password = config("db.password")
                metricsTrackerFactory = MetricsTrackerFactory { _, _ ->
                    object : IMetricsTracker {
                        override fun recordConnectionUsageMillis(elapsedBorrowedMillis: Long) {
                            Trace.addDbTime(elapsedBorrowedMillis)
                        }
                    }
                }
                HikariDataSource(this)
            }
        }

        val dbType = config<String>("db.type")

        val dialect = when (dbType.lowercase()) {
            "mysql" -> MySqlDialect()
            "postgres" -> PostgreSqlDialect()
            else -> throw Exception("Unknown Database type $dbType. Valid values are: MySQL or Postgres")
        }

        dataSource = createDataSource(config("db.jdbcUrl"))
        readWrite = KtormDatabse.connect(dataSource, dialect)

        val urlRead: List<String>? = configListOrNull("db.jdbcUrlRead")

        reads = if (urlRead?.isNotEmpty() == true && !Environment.isLocal) {
            urlRead.map { KtormDatabse.connect(createDataSource(it), dialect) }
        } else {
            listOf(readWrite)
        }
    }

    val connected: Boolean
        get() = try {
            query("select 1", result("u", Type.Boolean)).first().boolean("u")
        } catch (t: Throwable) {
            t.printStackTrace()
            false
        }

    fun execute(sql: String, vararg params: Param) = execute(sql, params.asList())

    fun execute(sql: String, params: List<Param>) = readWrite.useConnection { conn ->
        debug(sql, params)

        conn.prepareStatement(sql).use { statement ->
            addParams(params, statement)
            statement.executeUpdate()
        }
    }

    private fun debug(sql: String, params: List<Param>) {
        logger.debug { mapOf("sql" to sql, "params" to params).toJson() }
    }

    fun query(sql: String, vararg args: ParamOrResult) = query(sql, args.asList())
    fun query(sql: String, args: List<ParamOrResult>, mode: Mode = ReadWrite) = executeQuery(sql, args, mode)
    fun queryInt(sql: String, vararg params: Param) = queryInt(sql, params.asList())
    fun queryIds(query: String, vararg params: Param) = queryIds(query, params.asList())

    fun queryInt(sql: String, params: List<Param>, mode: Mode = ReadWrite) =
        query(sql, params + result("value", Type.Int), mode).map {
            it.int("value")
        }.firstOrNull() ?: 0

    fun queryIds(query: String, params: List<Param>, mode: Mode = ReadWrite) =
        query(query, params + result("id", Type.Long), mode).map { it.long("id") }

    fun queryReadOnly(sql: String, vararg args: ParamOrResult) = query(sql, args.asList(), Read)
    fun queryIntReadOnly(sql: String, vararg params: Param) = queryInt(sql, params.asList(), Read)
    fun queryIdsReadOnly(query: String, vararg params: Param) = queryIds(query, params.asList(), Read)

    private fun executeQuery(sql: String, args: List<ParamOrResult>, mode: Mode): List<Row> =
        mode.db.useConnection { conn ->
            val params = args.filterIsInstance<Param>()
            debug(sql, params)

            conn.prepareStatement(sql).use { statement ->
                addParams(params, statement)
                statement.executeQuery().let { rs ->
                    val results = mutableListOf<Row>()
                    val resultTypes = args.filterIsInstance<Result>()

                    while (rs.next()) {
                        resultTypes.mapIndexed { i, result ->
                            Pair(result.name, columnValue(rs, result, i + 1))
                        }.apply {
                            results.add(Row(toMap()))
                        }
                    }

                    results
                }
            }
        }

    private fun columnValue(rs: ResultSet, result: Result, index: Int) = when (rs.wasNull()) {
        true -> null
        else -> convertValue(result.type.toSqlType().getResult(rs, index))
    }

    private fun convertValue(value: Any?) = when (value) {
        is Timestamp -> value.toLocalDateTime()
        is Time -> value.toLocalTime()
        is Date -> value.toLocalDate()
        else -> value
    }

    private fun addParams(params: List<Param>, statement: PreparedStatement) = params.forEachIndexed { i, param ->
        val index = i + 1
        when (param.value) {
            null -> statement.setNull(index, param.type.toSqlType().typeCode)
            else ->
                @Suppress("UNCHECKED_CAST")
                (param.type.toSqlType() as SqlType<Any>).setParameter(statement, index, convertParam(param))
        }
    }

    private fun convertParam(param: Param) = when (param.value) {
        is LocalDateTime -> Timestamp.valueOf(param.value)
        is LocalDate -> Date.valueOf(param.value)
        is LocalTime -> Time.valueOf(param.value)
        else -> param.value
    }
}

fun <T> transaction(isolation: TransactionIsolation? = null, func: (Transaction) -> T) =
    Database.readWrite.useTransaction(isolation, func)
