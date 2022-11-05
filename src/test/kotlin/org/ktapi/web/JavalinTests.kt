package org.ktapi.web

import io.kotlintest.shouldBe
import org.ktapi.test.StringSpec

class JavalinTests : StringSpec({
    "routing" {
        WebServer.test() { _, client ->
            client.get("/1").body?.string() shouldBe "From1"
        }
    }

    "autorouting" {
        WebServer.test() { _, client ->
            client.get("/2").body?.string() shouldBe "From2"
        }
    }
})