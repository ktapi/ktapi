package org.ktapi.web

import io.javalin.Javalin
import io.javalin.core.security.AccessManager
import io.javalin.http.HttpCode
import io.javalin.plugin.json.JavalinJackson
import io.javalin.plugin.openapi.InitialConfigurationCreator
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.javalin.testtools.JavalinTest
import io.javalin.testtools.TestCase
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.ktapi.*
import org.ktapi.model.ValidationException
import org.ktapi.trace.Trace
import java.io.File

/**
 * This creates a Javalin server. Additional configuration, like adding routes can be added when instantiating this
 * class like this:
 *
 * ```
 * object WebServer : Javalin({
 *     routes {
 *        get("/myPath", myHandler)
 *     }
 * })
 *
 * WebServer.start()
 * ```
 * This class will automatically find instances of the Router interface in the same package at the WebServer class or
 * any package below it. So you may not need any initialization and can configure it like this:
 * ```
 * object WebServer : Javalin()
 * ```
 *
 * The created Javalin instance can be accessed at `WebServer.app`
 *
 * These following configurations are used by this object:
 * - web.openApi if this is `true` the root of the api will serve swagger and /openapi will server the OpenAPI json
 * - web.allowOpenApiInProd if this is `true` when the swagger will be served in production, the default is `false`
 * - web.traceExtraBuilder this is an instance of `WebTraceExtraBuilder` that allows you to add additional info to
 * web traces for each request
 * - web.corsOrigins comma separated list of domains that CORS should be enabled for, you can enter `*` to enable all origins
 * - web.accessManager an instance of `io.javalin.core.security.AccessManager` to be used for the Javalin instance
 * - web.serverPort the port to use for the web server, the default is `8080`
 */
abstract class Javalin(private val setup: Javalin.() -> Any = {}) {
    private val useOpenApi = config("web.openApi", true)
    private val allowOpenApiInProd = config("web.allowOpenApiInProd", false)
    private val traceExtraBuilder = config<WebTraceExtraBuilder>("web.traceExtraBuilder", EmptyWebTraceExtraBuilder)
    private val corsOrigins = configOrNull<String>("web.corsOrigins")
    private val port = config("web.serverPort", 8080)
    private var accessManager: AccessManager? = null
    private var currentApp: Javalin? = null
    private var routers: Sequence<Router>? = null

    constructor(accessManager: AccessManager, setup: Javalin.() -> Any = {}) : this(setup) {
        this.accessManager = accessManager
    }

    val app: Javalin
        get() {
            if (currentApp == null) currentApp = create()
            return currentApp!!
        }


    private fun create(): Javalin = Javalin.create {
        when (corsOrigins) {
            null -> Unit
            "*" -> it.enableCorsForAllOrigins()
            else -> it.enableCorsForOrigin(*corsOrigins.split(",").toTypedArray())
        }

        if (accessManager != null) {
            it.accessManager { handler, ctx, routeRoles ->
                if (useOpenApi && (ctx.path() == "/" || ctx.path() == "/openapi")) {
                    if (Environment.isNotProd || allowOpenApiInProd) {
                        handler.handle(ctx)
                    } else {
                        ctx.status(HttpCode.NOT_FOUND)
                    }
                } else {
                    accessManager?.manage(handler, ctx, routeRoles)
                }
            }
        }

        it.jsonMapper(JavalinJackson(Json.camelCaseMapper))

        if (useOpenApi) {
            val applicationInfo = InitialConfigurationCreator {
                OpenAPI().info(Info().version(Environment.version).description(Application.name))
            }

            val openApiOptions = OpenApiOptions(applicationInfo)
                .path("/openapi")
                .swagger(SwaggerOptions("/").title(Application.name))


            it.registerPlugin(OpenApiPlugin(openApiOptions))
        }
    }.apply {
        exception(ValidationException::class.java) { e, ctx ->
            ctx.json(e.validationErrors)
            ctx.status(HttpCode.BAD_REQUEST)
        }

        exception(Exception::class.java) { e, ctx ->
            e.printStackTrace()
            Application.ErrorReporter.report(e)
            ctx.status(HttpCode.INTERNAL_SERVER_ERROR)
        }

        before { context ->
            Application.ErrorReporter.setHttpInfo(context.req)
            Trace.clear()
            Trace.start("Web", context.path(), mapOf("url" to context.path()))
        }

        after { context ->
            Trace.finish(
                context.endpointHandlerPath(),
                traceExtraBuilder.build(context)
            )
        }

        autoRoute(this)
        setup(this)
    }

    fun start() {
        app.start(port)
    }

    private fun autoRoute(app: Javalin) {
        if (routers == null) {
            val resource = "/${this::class.qualifiedName?.replace(".", "/")}.class"
            val directory = File(this::class.java.getResource(resource).file).parentFile
            val base = directory.canonicalPath.substringBefore(resource.substringBeforeLast("/")) + "/"

            routers = directory.walk()
                .filter { it.isFile && !it.name.contains("$") && it.name.endsWith(".class") }
                .map { it.canonicalPath.removePrefix(base).removeSuffix(".class").replace('/', '.') }
                .map { Class.forName(it).kotlin.objectInstance }
                .filter { it is Router }
                .map { it as Router }
        }

        app.routes {
            routers!!.forEach { it.route() }
        }
    }

    fun test(testCase: TestCase) = JavalinTest.test(create(), testCase)
}
