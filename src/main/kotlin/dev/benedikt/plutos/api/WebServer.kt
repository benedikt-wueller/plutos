package dev.benedikt.plutos.api

import dev.benedikt.plutos.api.routing.*
import dev.benedikt.plutos.api.structure.*
import dev.benedikt.plutos.importers.Importer
import dev.benedikt.plutos.importers.statements.CommerzbankImporter
import dev.benedikt.plutos.importers.statements.SparkasseImporter
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.io.File

object WebServer {

    var lastPing: Long = System.currentTimeMillis()
    private lateinit var server: ApplicationEngine

    fun start(port: Int = 8143) {
        server = embeddedServer(CIO, port = port, module = Application::module)
        server.start(false)
    }

    fun wait() = server.start(true)

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
        allowMethod(HttpMethod.Put)
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
