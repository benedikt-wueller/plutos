package dev.benedikt.plutos.api.routing

import dev.benedikt.plutos.api.structure.ResourceObject
import dev.benedikt.plutos.api.structure.ResourceObjectBuilder
import dev.benedikt.plutos.api.structure.respondDocument
import dev.benedikt.plutos.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.categoryRouting() {
    route("/categories") {
        get {
            val entities = transaction { Categories.selectAll().map(ResultRow::toCategory) }
            call.respondDocument(HttpStatusCode.OK, entities.map(Category::toResourceObject))
        }

        route("{categoryId}") {
            get {
                val id = call.parameters.getOrFail("categoryId").toIntOrNull()
                val entity = transaction { id?.let { Categories.select { Categories.id eq it }.firstOrNull() }?.toCategory() }
                call.respondDocument(
                    if (entity == null) HttpStatusCode.NotFound else HttpStatusCode.OK,
                    entity?.toResourceObject()
                )
            }

            delete {
                val id = call.parameters.getOrFail("categoryId").toIntOrNull()
                if (id == null) {
                    call.respondDocument(HttpStatusCode.NotFound)
                    return@delete
                }

                transaction { Categories.deleteWhere { Categories.id eq id } }
                call.respondDocument(HttpStatusCode.OK)
            }

            route("/patterns") {
                get {
                }

                post {

                }

                delete("{patternId}") {

                }
            }

            route("/statements") {
                get {

                }
            }
        }
    }
}

fun Category.toResourceObject(): ResourceObject<Category> {
    val entity = this
    return transaction {
        val patterns = CategoryPatterns.slice(CategoryPatterns.patternId).select { CategoryPatterns.categoryId eq entity.id }.map { it[CategoryPatterns.patternId].value }
        val statements = Statements.slice(Statements.id).select { Statements.categoryId eq entity.id }.map { it[Statements.id].value }
        return@transaction ResourceObjectBuilder("categories", entity.id!!, entity)
            .relationship("patterns", "patterns", patterns)
            .relationship("statements", "statements", statements)
            .build()
    }
}
