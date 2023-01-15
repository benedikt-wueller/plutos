package dev.benedikt.plutos.api.routing

import dev.benedikt.plutos.api.structure.*
import dev.benedikt.plutos.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.categoryRouting() {
    route("/categories") {
        // Get all categories.
        get {
            try {
                val entities = transaction { Categories.selectAll().map(ResultRow::toCategory) }
                call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        // Create new category.
        post {
            val data = call.receive<ModelWrapper<Category>>().data
            val entity = transaction {
                if (data.attributes.default) {
                    Categories.update { it[default] = false }
                }
                return@transaction Categories.insert(data)
            }
            call.respondDocument(HttpStatusCode.Created, entity.toResourceObject(), includeLinks = false)
        }

        route("{categoryId}") {
            // Get category.
            get {
                val id = call.parameters.getOrFail("categoryId").toIntOrNull()
                val entity = transaction { id?.let { Categories.select { Categories.id eq it }.firstOrNull() }?.toCategory() }

                if (entity == null) {
                    call.respondCategoryNotFound()
                    return@get
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Update category.
            put {
                val id = call.parameters.getOrFail("categoryId").toIntOrNull()
                val data = call.receive<ModelWrapper<Category>>().data

                val entity = data.copy(id = id)
                val success = transaction {
                    if (entity.attributes.default) {
                        Categories.update { it[default] = false }
                    }

                    if (!Categories.update(entity)) {
                        rollback()
                        return@transaction false
                    }

                    val defaultExists = Categories.select { Categories.default eq true }.any()
                    if (defaultExists) return@transaction true

                    Categories.update({ Categories.id eq id }) { it[default] = true }
                    return@transaction true
                }

                if (!success) {
                    call.respondCategoryNotFound()
                    return@put
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Delete category.
            delete {
                val id = call.parameters.getOrFail("categoryId").toIntOrNull()
                if (id == null) {
                    call.respondDocument(HttpStatusCode.NotFound)
                    return@delete
                }

                val success = transaction {
                    val success = Categories.deleteWhere { (Categories.id eq id) and (default eq false) } > 0
                    if (!success) return@transaction false

                    val patterns = CategoryPatterns.slice(CategoryPatterns.patternId).select { CategoryPatterns.categoryId eq id }.map { it[CategoryPatterns.patternId] }
                    Patterns.deleteWhere { Patterns.id inList patterns }
                    CategoryPatterns.deleteWhere { categoryId eq id }

                    val defaultCategoryId = Categories.slice(Categories.id).select { Categories.default eq true }.first()[Categories.id]
                    Statements.update({ Statements.categoryId eq id }) { it[categoryId] = defaultCategoryId.value }

                    return@transaction true
                }

                if (!success) {
                    call.respondError(HttpStatusCode.BadRequest, ErrorObject("category_not_deletable", "The category does not exist or is the default category."))
                    return@delete
                }

                call.respondDocument(HttpStatusCode.OK)
            }

            route("/patterns") {
                // Get patterns associated to category.
                get {
                    val id = call.parameters.getOrFail("categoryId").toIntOrNull()
                    val entities = transaction {
                        CategoryPatterns.leftJoin(Patterns)
                            .select { CategoryPatterns.categoryId eq id }
                            .map(ResultRow::toCategoryPattern)
                    }
                    call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
                }

                // Add pattern to category.
                post {
                    val id = call.parameters.getOrFail("categoryId").toIntOrNull()
                    val pattern = call.receive<ModelWrapper<CategoryPattern>>().data

                    val entity = transaction {
                        val category = Categories.slice(Categories.id).select { Categories.id eq id }.firstOrNull() ?: return@transaction null
                        return@transaction Patterns.insertCategoryPattern(pattern, category[Categories.id].value)
                    }

                    if (entity == null) {
                        call.respondCategoryNotFound()
                        return@post
                    }

                    call.respondDocument(HttpStatusCode.Created, entity.toResourceObject())
                }
            }

            route("/statements") {
                // Get statements associated to category.
                get {
                    val id = call.parameters.getOrFail("categoryId").toIntOrNull()
                    val entities = transaction { Statements.select { Statements.categoryId eq id }.map(ResultRow::toStatement) }
                    call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
                }
            }
        }
    }
}

suspend fun ApplicationCall.respondCategoryNotFound() {
    this.respondError(HttpStatusCode.NotFound, ErrorObject("category_not_found", "The category does not exist."))
}
