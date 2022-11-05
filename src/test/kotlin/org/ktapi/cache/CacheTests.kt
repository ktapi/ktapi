package org.ktapi.cache

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.ktapi.typeRef

class CacheTests : StringSpec() {
    private val impls = listOf(InMemoryCache, Redis)

    data class Data(val value: String)

    init {
        "connected works" {
            impls.forEach {
                it.connected shouldBe true
            }
        }

        "reading missing key returns null" {
            impls.forEach {
                it.get("missing") shouldBe null
            }
        }

        "reading set key" {
            impls.forEach {
                it.set("myKey", "myValue")

                it.get("myKey") shouldBe "myValue"
            }
        }

        "deleting key" {
            impls.forEach {
                it.set("anotherKey", "value")

                it.delete("anotherKey")

                it.get("anotherKey") shouldBe null
            }
        }

        "updating key" {
            impls.forEach {
                it.set("toUpdate", "oldValue")

                it.update("toUpdate", "newValue")

                it.get("toUpdate") shouldBe "newValue"
            }
        }

        "get as" {
            impls.forEach {
                it.set("data", Data("aValue"))

                val data = it.getAs<Data>("data", typeRef())

                data?.value shouldBe "aValue"
            }
        }

        "get as inline" {
            impls.forEach {
                it.set("data", Data("aValue"))

                val data: Data? = it.getAs("data")

                data?.value shouldBe "aValue"
            }
        }
    }
}