package org.ktapi.db

import org.flywaydb.core.Flyway
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() = Migration.run()

/**
 * This object allows you to create or run a Database migration.
 */
object Migration {
    fun create(
        rootDir: File,
        name: String?,
        isKotlin: Boolean,
        repeatable: Boolean,
        baseline: Boolean,
        undo: String?
    ): File {
        val replaceRegex = "[^a-zA-Z0-9.\\-]".toRegex()
        val convertedName = (name ?: "new_migration").replace(replaceRegex, "_")

        val prefix = when {
            baseline -> "B"
            repeatable -> "R"
            undo != null -> "U"
            else -> "M"
        }

        val number = when {
            repeatable -> ""
            undo != null -> undo
            else -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        }
        val suffix = if (isKotlin) "kt" else "sql"

        val fileName = "$prefix${number}_$convertedName"
        val baseFolder = if (isKotlin) "kotlin" else "resources"
        val fullPath = "$baseFolder/migration/$fileName.$suffix"
        val file = File(rootDir, fullPath)

        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        when {
            file.exists() -> throw Exception("Migration file already exists: $fullPath")
            file.createNewFile() -> {
                if (isKotlin) {
                    file.writeText(
                        """
                        package migration
                        
                        import org.ktapi.db.KotlinMigration
    
                        class $fileName : KotlinMigration() {
                            override fun migrate() {
                            }

                            override fun getChecksum() = ${(Math.random() * Int.MAX_VALUE).toInt()}
                        }
                    """.trimIndent()
                    )
                }
                println("Created migration at $fullPath")
            }

            else -> println("Failed to created migration at $fullPath")
        }

        return file
    }

    fun run() {
        Flyway.configure()
            .locations("classpath:migration")
            .sqlMigrationSeparator("_")
            .sqlMigrationPrefix("M")
            .ignoreMigrationPatterns("*:missing")
            .outOfOrder(true)
            .dataSource(Database.dataSource)
            .load()
            .migrate()
    }
}