package dev.benedikt.plutos.api.routing

import dev.benedikt.plutos.api.structure.*
import dev.benedikt.plutos.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.tagPatternRouting() {
    route("/tagPatterns") {
        // Get all tag patterns.
        get {
            val entities = transaction { TagPatterns.leftJoin(Patterns).selectAll().map(ResultRow::toTagPattern) }
            call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
        }

        route("{id}") {
            // Get tag pattern.
            get {
                val id = call.parameters.getOrFail("id").toIntOrNull()
                val entity = transaction { id?.let { TagPatterns.leftJoin(Patterns).select { TagPatterns.id eq it }.firstOrNull() }?.toTagPattern() }

                if (entity == null) {
                    call.respondTagPatternNotFound()
                    return@get
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Update tag pattern.
            patch {
                val id = call.parameters.getOrFail("id").toIntOrNull()
                val data = call.receive<ModelWrapper<Pattern>>().data.copy(id = id)

                val entity = transaction {
                    val tagPattern = TagPatterns.select { TagPatterns.id eq id }.firstOrNull() ?: return@transaction null
                    val entity = data.toTagPattern(tagPattern[TagPatterns.patternId].value, tagPattern[TagPatterns.tagId].value)
                    if (!Patterns.updatePattern(entity.copy(id = tagPattern[TagPatterns.patternId].value))) return@transaction null
                    return@transaction entity
                }

                if (entity == null) {
                    call.respondTagPatternNotFound()
                    return@patch
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Delete tag pattern.
            delete {
                val id = call.parameters.getOrFail("id").toIntOrNull()
                if (id == null) {
                    call.respondTagPatternNotFound()
                    return@delete
                }

                val success = transaction {
                    val patternId = TagPatterns.slice(TagPatterns.patternId)
                        .select { TagPatterns.id eq id }
                        .firstOrNull()?.get(TagPatterns.patternId)?.value

                    Patterns.deleteWhere { Patterns.id eq patternId } > 0
                    return@transaction patternId != null && TagPatterns.deleteWhere { TagPatterns.id eq id } > 0
                }

                if (!success) {
                    call.respondTagPatternNotFound()
                    return@delete
                }

                call.respondDocument(HttpStatusCode.OK)
            }

            route("/tag") {
                // Get tag associated to tag pattern.
                get {
                    val id = call.parameters.getOrFail("id").toIntOrNull()
                    val tag = transaction { TagPatterns.leftJoin(Tags).select { TagPatterns.id eq id }.firstOrNull()?.toTag()?.toResourceObject() }
                    call.respondDocument(HttpStatusCode.OK, tag)
                }
            }
        }
    }
}

suspend fun ApplicationCall.respondTagPatternNotFound() {
    this.respondError(HttpStatusCode.NotFound, ErrorObject("tag_pattern_not_found", "The tag pattern does not exist."))
}
