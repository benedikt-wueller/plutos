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

fun Route.tagRouting() {
    route("/tags") {
        // Get all tags.
        get {
            try {
                val entities = transaction { Tags.selectAll().map(ResultRow::toTag) }
                call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        // Create new tag.
        post {
            val data = call.receive<ResourceWrapper<TagResourceObject>>().data
            var entity = Model(Tag.type, null, data.attributes)

            val categoryRelationship = data.relationships["category"]?.let { it as? SingleRelationshipObject }
            if (categoryRelationship != null) {
                val categoryId = categoryRelationship.data?.id
                if (categoryId != null) {
                    val category = transaction { Categories.select { Categories.id eq categoryId } }
                    if (category == null) {
                        call.respondCategoryNotFound()
                        return@post
                    }
                    entity.attributes.categoryId = categoryId
                } else {
                    entity.attributes.categoryId = null
                }
            }

            entity = transaction { Tags.insert(entity) }
            call.respondDocument(HttpStatusCode.Created, entity.toResourceObject(), includeLinks = false)
        }

        route("{tagId}") {
            // Get tag.
            get {
                val id = call.parameters.getOrFail("tagId").toIntOrNull()
                val entity = transaction { id?.let { Tags.select { Tags.id eq it }.firstOrNull() }?.toTag() }

                if (entity == null) {
                    call.respondTagNotFound()
                    return@get
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Update tag.
            patch {
                val id = call.parameters.getOrFail("tagId").toIntOrNull()
                val data = call.receive<ResourceWrapper<TagResourceObject>>().data

                if (id == null) {
                    call.respondTagNotFound()
                    return@patch
                }

                val entity = Model(Tag.type, id, data.attributes)

                val categoryRelationship = data.relationships["category"]?.let { it as? SingleRelationshipObject }
                if (categoryRelationship != null) {
                    val categoryId = categoryRelationship.data?.id
                    if (categoryId != null) {
                        val category = transaction { Categories.select { Categories.id eq categoryId } }
                        if (category == null) {
                            call.respondCategoryNotFound()
                            return@patch
                        }
                        entity.attributes.categoryId = categoryId
                    } else {
                        entity.attributes.categoryId = null
                    }
                }

                val success = transaction {
                    Tags.update(entity)
                }

                if (!success) {
                    call.respondTagNotFound()
                    return@patch
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Delete tag.
            delete {
                val id = call.parameters.getOrFail("tagId").toIntOrNull()
                if (id == null) {
                    call.respondTagNotFound()
                    return@delete
                }

                val success = transaction {
                    val success = Tags.deleteWhere { (Tags.id eq id) } > 0
                    if (!success) return@transaction false

                    val patterns = TagPatterns.slice(TagPatterns.patternId).select { TagPatterns.tagId eq id }.map { it[TagPatterns.patternId] }
                    Patterns.deleteWhere { Patterns.id inList patterns }
                    TagPatterns.deleteWhere { tagId eq id }
                    StatementTags.deleteWhere { tagId eq id }
                    return@transaction true
                }

                if (!success) {
                    call.respondTagNotFound()
                    return@delete
                }

                call.respondDocument(HttpStatusCode.OK)
            }

            route("/patterns") {
                // Get patterns associated to tag.
                get {
                    val id = call.parameters.getOrFail("tagId").toIntOrNull()
                    val entities = transaction {
                        TagPatterns.leftJoin(Patterns)
                            .select { TagPatterns.tagId eq id }
                            .map(ResultRow::toTagPattern)
                    }
                    call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
                }

                // Add pattern to tag.
                post {
                    val id = call.parameters.getOrFail("tagId").toIntOrNull()
                    val pattern = call.receive<ModelWrapper<TagPattern>>().data

                    val entity = transaction {
                        val tag = Tags.slice(Tags.id).select { Tags.id eq id }.firstOrNull() ?: return@transaction null
                        return@transaction Patterns.insertTagPattern(pattern, tag[Tags.id].value)
                    }

                    if (entity == null) {
                        call.respondTagNotFound()
                        return@post
                    }

                    call.respondDocument(HttpStatusCode.Created, entity.toResourceObject())
                }
            }

            route("/statements") {
                // Get statements associated to tag.
                get {
                    val id = call.parameters.getOrFail("tagId").toIntOrNull()
                    val tags = transaction { StatementTags.leftJoin(Statements).select { StatementTags.tagId eq id }.map(ResultRow::toStatement).map { it.toResourceObject() } }
                    call.respondDocument(HttpStatusCode.OK, tags)
                }
            }

            route("/category") {
                // Get category associated to tag.
                get {
                    val id = call.parameters.getOrFail("tagId").toIntOrNull()
                    val category = transaction { Tags.leftJoin(Categories).select { Tags.id eq id }.firstOrNull()?.toCategory()?.toResourceObject() }
                    call.respondDocument(HttpStatusCode.OK, category)
                }
            }
        }
    }
}

suspend fun ApplicationCall.respondTagNotFound() {
    this.respondError(HttpStatusCode.NotFound, ErrorObject("tag_not_found", "The tag does not exist."))
}
