package org.ktapi.db

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.ktapi.db.Database.Type
import org.ktapi.db.Database.param
import org.ktapi.db.Database.result
import java.io.File

class MigrationTest : StringSpec({
    "create sql migration" {
        val root = File("src/test")
        val sqlMigration = Migration.create(
            rootDir = root,
            name = "setup",
            isKotlin = false,
            repeatable = false,
            baseline = false,
            undo = null
        )

        val kotlinMigration = Migration.create(
            rootDir = root,
            name = "setup",
            isKotlin = true,
            repeatable = false,
            baseline = false,
            undo = null
        )

        sqlMigration.exists() shouldBe true
        sqlMigration.delete()
        kotlinMigration.exists() shouldBe true
        kotlinMigration.delete()
    }

    "run migrations" {
        Database.execute("drop table if exists something_else")
        Database.execute("drop table if exists something")
        Database.execute("drop table if exists flyway_schema_history")
        Migration.run()

        val results =
            Database.query("select name from something where id = ?", param(1), result("name", Type.String))

        results.size shouldBe 1
        results.first().string("name") shouldBe "FirstValue"
    }
})