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
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Route.statementRouting() {
    route("/statements") {
        // Get all statements.
        get {
            val from = call.request.queryParameters["from"]?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) } ?: LocalDate.MIN
            val to = call.request.queryParameters["to"]?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) } ?: LocalDate.now()

            val entities = transaction {
                Statements
                    .select { (Statements.valueDate greaterEq from) and (Statements.valueDate lessEq to) }
                    .map(ResultRow::toStatement)
            }

            call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
        }

        // Create statement.
        post {
            createOrUpdateStatement(call)
        }

        delete {
            transaction {
                Statements.deleteAll()
                StatementTags.deleteAll()
            }
            call.respondDocument(HttpStatusCode.OK)
        }

        route("{statementId}") {
            // Get statement.
            get {
                val id = call.parameters.getOrFail("statementId").toIntOrNull()
                val entity = transaction { id?.let { Statements.select { Statements.id eq it }.firstOrNull() }?.toStatement() }

                if (entity == null) {
                    call.respondStatementNotFound()
                    return@get
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Update statement.
            patch {
                createOrUpdateStatement(call)
            }

            // Delete statement.
            delete {
                val id = call.parameters.getOrFail("statementId").toIntOrNull()
                if (id == null) {
                    call.respondDocument(HttpStatusCode.NotFound)
                    return@delete
                }

                val success = transaction {
                    StatementTags.deleteWhere { tagId eq id }
                    Statements.deleteWhere { Statements.id eq id } > 0
                }
                if (!success) {
                    call.respondStatementNotFound()
                    return@delete
                }

                call.respondDocument(HttpStatusCode.OK)
            }

            route("/category") {
                // Get category assigned to statement.
                get {
                    val id = call.parameters.getOrFail("id").toIntOrNull()
                    val category = transaction {
                        val categoryId = Statements.slice(Statements.categoryId).select { Statements.id eq id }.firstOrNull()?.get(Statements.categoryId) ?: return@transaction null
                        return@transaction Categories.select { Categories.id eq categoryId }.firstOrNull()?.toCategory()?.toResourceObject()
                    }
                    call.respondDocument(HttpStatusCode.OK, category)
                }
            }

            route("/account") {
                // Get account assigned to statement.
                get {
                    val id = call.parameters.getOrFail("id").toIntOrNull()
                    val account = transaction {
                        val accountId = Statements.slice(Statements.accountId).select { Statements.id eq id }.firstOrNull()?.get(Statements.accountId) ?: return@transaction null
                        return@transaction Accounts.select { Accounts.id eq accountId }.firstOrNull()?.toAccount()?.toResourceObject()
                    }
                    call.respondDocument(HttpStatusCode.OK, account)
                }
            }

            route("/tags") {
                // Get tags assigned to statement.
                get {
                    val id = call.parameters.getOrFail("id").toIntOrNull()
                    val tags = transaction { StatementTags.leftJoin(Tags).select { StatementTags.statementId eq id }.map(ResultRow::toTag).map { it.toResourceObject() } }
                    call.respondDocument(HttpStatusCode.OK, tags)
                }
            }
        }
    }
}

suspend fun createOrUpdateStatement(call: ApplicationCall) {
    val idParameter = call.parameters["statementId"]
    val data = call.receive<ResourceWrapper<StatementResourceObject>>().data

    if (idParameter != null) {
        val id = call.parameters.getOrFail("statementId").toIntOrNull()
        if (id == null) {
            call.respondStatementNotFound()
            return
        }
        data.id = id
    }

    val tagIdentifiers = data.relationships["tags"]?.let { it as? MultiRelationshipObject }?.data
    val tagIds = tagIdentifiers?.map { it.id }
        ?: transaction {
            val statement = Statements.select { Statements.id eq data.id }.first().toStatement().attributes
            val patterns = TagPatterns.leftJoin(Patterns).selectAll().map(ResultRow::toTagPattern)
            determineTagIds(statement, patterns)
        }

    val categoryId = data.relationships["category"]?.let { it as? SingleRelationshipObject }?.data?.id
    if (categoryId == null || categoryId < 0) {
        data.attributes.manualCategory = false
        data.attributes.categoryId = transaction {
            val categoryIds = StatementTags.leftJoin(Tags).select { StatementTags.id inList tagIds }.mapNotNull { it[Tags.categoryId]?.value }
            val patterns = CategoryPatterns.leftJoin(Patterns).selectAll().map(ResultRow::toCategoryPattern)
            val defaultCategoryId = Categories.slice(Categories.id).select { Categories.default eq true }.first()[Categories.id]
            determineCategoryId(data.attributes, patterns, defaultCategoryId.value, categoryIds)
        }
    } else {
        data.attributes.manualCategory = true
        data.attributes.categoryId = categoryId
    }

    val accountId = data.relationships["account"]?.let { it as? SingleRelationshipObject }?.data?.id
    if (data.id != null && (accountId == null || accountId < 0)) {
        data.attributes.accountId = transaction { Statements.select { Statements.id eq data.id }.first()[Accounts.id].value }
    } else {
        data.attributes.accountId = accountId
    }

    val entity = transaction {
        if (!Accounts.select { Accounts.id eq data.attributes.accountId }.any()) return@transaction null
        if (!Categories.select { Categories.id eq data.attributes.categoryId }.any()) return@transaction null

        val tagsExist = Tags.select { Tags.id inList tagIds }.count() == tagIds.size.toLong()
        if (!tagsExist) return@transaction null

        data.attributes.updateIdHash()
        data.attributes.updateContentHash()
        data.attributes.manualTags = tagIdentifiers != null

        val model = Model(Statement.type, data.id, data.attributes)
        val entity = if (model.id == null) {
            Statements.insert(model)
        } else if (Statements.update(model)) {
            model
        } else return@transaction null

        StatementTags.deleteWhere { statementId eq entity.id!! }
        tagIds.forEach { id ->
            StatementTags.insert {
                it[statementId] = entity.id!!
                it[tagId] = id
            }
        }

        return@transaction entity
    }

    if (entity == null) {
        call.respondError(HttpStatusCode.BadRequest, ErrorObject("invalid_statement_request", "The account, category and/or tags do not exist."))
        return
    }

    call.respondDocument(HttpStatusCode.OK, entity.toResourceObject(), includeLinks = data.id != null)
}

suspend fun ApplicationCall.respondStatementNotFound() {
    this.respondError(HttpStatusCode.NotFound, ErrorObject("statement_not_found", "The statement does not exist."))
}
