package org.ktapi.model

import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import org.ktapi.db.Migration
import org.ktapi.test.DbStringSpec

class EntityTests : DbStringSpec() {
    override fun beforeSpec(spec: Spec) = Migration.run()

    init {
        "can load by id" {
            val s = SomethingRepo.findById(1)

            s?.name shouldBe "FirstValue"
        }

        "lazy loaded value" {
            val s = Something {}

            val value = s.aLazyValue

            value shouldNotBe null
            value shouldBe s.aLazyValue
            s.clearLazyLoad("aLazyValue")
            value shouldNotBe s.aLazyValue
        }

        "lazy load object" {
            val s = SomethingElseRepo.findById(1)

            s?.something?.name shouldBe "FirstValue"
        }

        "validation throws exception when invalid" {
            val s = Something { name = "" }

            shouldThrow<ValidationException> {
                s.validate()
            }
        }

        "validation does nothing when valid" {
            val s = Something { name = "blah" }

            s.validate()
        }
    }
}