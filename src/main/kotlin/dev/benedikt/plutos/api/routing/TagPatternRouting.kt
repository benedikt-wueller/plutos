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

fun Route.categoryPatternRouting() {
    route("/categoryPatterns") {
        // Get all category patterns.
        get {
            val entities = transaction { CategoryPatterns.leftJoin(Patterns).selectAll().map(ResultRow::toCategoryPattern) }
            call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
        }

        route("{id}") {
            // Get category pattern.
            get {
                val id = call.parameters.getOrFail("id").toIntOrNull()
                val entity = transaction { id?.let { CategoryPatterns.leftJoin(Patterns).select { CategoryPatterns.id eq it }.firstOrNull() }?.toCategoryPattern() }

                if (entity == null) {
                    call.respondCategoryPatternNotFound()
                    return@get
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Update category pattern.
            put {
                val id = call.parameters.getOrFail("id").toIntOrNull()
                val data = call.receive<ModelWrapper<Pattern>>().data.copy(id = id)

                val entity = transaction {
                    val categoryPattern = CategoryPatterns.select { CategoryPatterns.id eq id }.firstOrNull() ?: return@transaction null
                    val entity = data.toCategoryPattern(categoryPattern[CategoryPatterns.id].value, categoryPattern[CategoryPatterns.categoryId].value)
                    if (!Patterns.updatePattern(entity)) return@transaction null
                    return@transaction entity
                }

                if (entity == null) {
                    call.respondCategoryPatternNotFound()
                    return@put
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Delete category pattern.
            delete {
                val id = call.parameters.getOrFail("id").toIntOrNull()
                if (id == null) {
                    call.respondDocument(HttpStatusCode.NotFound)
                    return@delete
                }

                val success = transaction {
                    val patternId = CategoryPatterns.slice(CategoryPatterns.patternId)
                        .select { CategoryPatterns.id eq id }
                        .firstOrNull()?.get(CategoryPatterns.patternId)?.value

                    Patterns.deleteWhere { Patterns.id eq patternId } > 0
                    return@transaction patternId != null && CategoryPatterns.deleteWhere { CategoryPatterns.id eq id } > 0
                }

                if (!success) {
                    call.respondCategoryPatternNotFound()
                    return@delete
                }

                call.respondDocument(HttpStatusCode.OK)
            }

            route("/category") {
                // Get category associated to category pattern.
                get {
                    val id = call.parameters.getOrFail("id").toIntOrNull()
                    val category = transaction { CategoryPatterns.leftJoin(Categories).select { CategoryPatterns.id eq id }.firstOrNull()?.toCategory()?.toResourceObject() }
                    call.respondDocument(HttpStatusCode.OK, category)
                }
            }
        }
    }
}

suspend fun ApplicationCall.respondCategoryPatternNotFound() {
    this.respondError(HttpStatusCode.NotFound, ErrorObject("category_pattern_not_found", "The category pattern does not exist."))
}
