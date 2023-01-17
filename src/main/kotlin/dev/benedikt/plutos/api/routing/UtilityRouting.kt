package dev.benedikt.plutos.api.routing

import dev.benedikt.plutos.api.WebServer
import dev.benedikt.plutos.importers.Importer
import dev.benedikt.plutos.importers.ImporterService
import dev.benedikt.plutos.importers.statements.CommerzbankImporter
import dev.benedikt.plutos.importers.statements.SparkasseImporter
import dev.benedikt.plutos.models.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.utilityRouting() {
    route("/utils") {
        post("keepalive") {
            WebServer.lastPing = System.currentTimeMillis()
            call.respond(HttpStatusCode.NoContent)
        }

        post("applyPatterns") {
            transaction {
                val statements = Statements.selectAll().map(ResultRow::toStatement)
                applyCategoryAndTags(statements)
            }
            call.respond(HttpStatusCode.NoContent)
        }

        get("importers") {
            val json = Json {
                serializersModule = SerializersModule {
                    polymorphic(Importer::class) {
                        subclass(SparkasseImporter::class)
                        subclass(CommerzbankImporter::class)
                    }
                }
            }.encodeToString(ImporterService.all())

            call.respondText(json, contentType = ContentType.Application.Json)
        }

        post("import/{importer}") {
            val importer = call.parameters["importer"]?.let { ImporterService.find(it) }
            if (importer == null) {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    part.streamProvider().use { importer.import(it) }
                    call.respond(HttpStatusCode.NoContent)
                }
                part.dispose()
            }
        }
    }
}
