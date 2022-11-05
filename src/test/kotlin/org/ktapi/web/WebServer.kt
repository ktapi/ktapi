package org.ktapi.web

import io.javalin.apibuilder.ApiBuilder.get

object WebServer : Javalin({
    routes {
        get("/1") { ctx -> ctx.result("From1") }
    }
})

object MyRoutes : Router {
    override fun route() {
        get("/2") { ctx -> ctx.result("From2") }
    }
}