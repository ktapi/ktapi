package org.ktapi.db

import io.kotlintest.Spec
import io.kotlintest.shouldBe
import org.ktapi.db.Database.Type
import org.ktapi.db.Database.param
import org.ktapi.db.Database.paramN
import org.ktapi.db.Database.result
import org.ktapi.test.DbStringSpec
import org.ktapi.toMillisUtc

class DatabseTests : DbStringSpec() {
    override fun beforeSpec(spec: Spec) = Migration.run()

    init {
        "connected" {
            Database.connected shouldBe true
        }

        "query for int" {
            val count = Database.queryInt("select count(*) from something")

            (count > 0) shouldBe true
        }

        "query" {
            Database.execute("insert into something (name) values ('anotherName')")

            val results =
                Database.query(
                    "select name from something where name = ?",
                    param("anotherName"),
                    result("name", Type.String)
                )

            results.size shouldBe 1
            results.first().string("name") shouldBe "anotherName"
        }

        "query for ids" {
            val ids = Database.queryIds("select id from something order by id")

            ids.isNotEmpty() shouldBe true
            ids.first() shouldBe 1
        }

        "query with null param" {
            val nullName: String? = null

            val ids = Database.queryIds("select id from something where name = ?", paramN(nullName))

            ids.isEmpty() shouldBe true
        }

        "query with null result" {
            val results = Database.query(
                "select name, value from something where id = 1",
                result("name", Type.String),
                result("value", Type.String)
            )

            results.size shouldBe 1
            results.first().string("name") shouldBe "FirstValue"
            results.first().stringOrNull("value") shouldBe null
        }

        "query for date time" {
            val results =
                Database.query("select created_at from something where id = 1", result("createdAt", Type.LocalDateTime))

            results.size shouldBe 1
            (results.first().localDateTime("createdAt").toMillisUtc() > 0) shouldBe true
        }

        "read only query" {
            val ids = Database.queryIdsReadOnly("select id from something order by id")

            ids.isNotEmpty() shouldBe true
        }
    }
}
