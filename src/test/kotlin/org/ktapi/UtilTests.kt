package org.ktapi

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

class UtilTests : StringSpec({
    "resourceExists return false for nonexistent resource" {
        "blah".resourceExists() shouldBe false
    }

    "resourceExists return true for existing resource" {
        "app.yml".resourceExists() shouldBe true
    }

    "resourceAsString return content of resource" {
        "app.yml".resourceAsString()?.contains("aNumberList") shouldBe true
    }

    "resourceAsString return null for missing resource" {
        "non-exising.json".resourceAsString() shouldBe null
    }
    
    "report and throw error" {
        val exception = shouldThrow<Exception> {
            reportAndThrow {
                throw Exception("hello")
            }
        }

        exception.message shouldBe "hello"
    }

    "report and swallow error" {
        reportAndSwallow {
            throw Exception("still passing test")
        }
    }
})
