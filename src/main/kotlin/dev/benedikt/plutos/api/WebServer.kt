package dev.benedikt.plutos.api

import dev.benedikt.plutos.api.routing.*
import dev.benedikt.plutos.api.structure.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.route
import kotlinx.serialization.json.Json

object WebServer {

    var lastPing: Long = System.currentTimeMillis()
    private lateinit var server: ApplicationEngine

    fun run(port: Int = 8143) {
        server = embeddedServer(CIO, port = port, module = Application::module)
        server.start(true)
    }

    fun stop() = server.stop()

}

fun Application.module() {
    configureCors()
    configureContentNegotiation()
    configureMiddleware()
    configureRouting()
}

fun Application.configureCors() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
    }
}

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
}

fun Application.configureMiddleware() {
    val plugin = createApplicationPlugin("middleware") {
        on(CallFailed) { _, cause ->
            cause.printStackTrace()
        }
        onCall { call ->
            println(call.request.uri)
            val origin = call.request.origin
            ResourceBuilderSettings.baseUrl = "${origin.scheme}://${origin.serverHost}:${origin.serverPort}/api/v1"
        }
    }
    install(plugin)
}

fun Application.configureRouting() {
    routing {
        route("/api/v1") {
            accountRouting()
            categoryRouting()
            categoryPatternRouting()
            statementRouting()
            tagRouting()
            tagPatternRouting()
            utilityRouting()
        }
    }
}
