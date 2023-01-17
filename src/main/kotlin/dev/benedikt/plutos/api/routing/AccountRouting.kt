package dev.benedikt.plutos.api.routing

import dev.benedikt.plutos.api.structure.*
import dev.benedikt.plutos.models.*
import dev.benedikt.plutos.models.Statements.accountId
import dev.benedikt.plutos.models.update
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.accountRouting() {
    route("/accounts") {
        // Get all accounts.
        get {
            val entities = transaction { Accounts.selectAll().map(ResultRow::toAccount) }
            call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
        }

        // Create new account.
        post {
            val data = call.receive<ModelWrapper<Account>>().data
            val entity = transaction { Accounts.insert(data) }
            call.respondDocument(HttpStatusCode.Created, entity.toResourceObject(), includeLinks = false)
        }

        route("{accountId}") {
            // Get account.
            get {
                val id = call.parameters.getOrFail("accountId").toIntOrNull()
                val entity = transaction { id?.let { Accounts.select { Accounts.id eq it }.firstOrNull() }?.toAccount() }

                if (entity == null) {
                    call.respondAccountNotFound()
                    return@get
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Update account.
            patch {
                val id = call.parameters.getOrFail("accountId").toIntOrNull()

                val data = call.receive<ModelWrapper<Account>>().data.copy(id = id)
                val success = transaction { Accounts.update(data) }

                if (!success) {
                    call.respondAccountNotFound()
                    return@patch
                }

                call.respondDocument(HttpStatusCode.OK, data.toResourceObject())
            }

            // Delete account.
            delete {
                val id = call.parameters.getOrFail("accountId").toIntOrNull()
                if (id == null) {
                    call.respondDocument(HttpStatusCode.NotFound)
                    return@delete
                }

                val success = transaction {
                    val statementIds = Statements.select { accountId eq id }.map { it[Statements.id] }
                    StatementTags.deleteWhere { statementId inList statementIds }

                    Statements.deleteWhere { accountId eq id }
                    Accounts.deleteWhere { Accounts.id eq id } > 0
                }

                if (!success) {
                    call.respondAccountNotFound()
                    return@delete
                }

                call.respondDocument(HttpStatusCode.OK)
            }

            route("/statements") {
                // Get statements associated to account.
                get {
                    val id = call.parameters.getOrFail("accountId").toIntOrNull()
                    val entities = transaction { Statements.select { Statements.accountId eq id }.map(ResultRow::toStatement) }
                    call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
                }
            }
        }
    }
}

suspend fun ApplicationCall.respondAccountNotFound() {
    this.respondError(HttpStatusCode.NotFound, ErrorObject("account_not_found", "The account does not exist."))
}
