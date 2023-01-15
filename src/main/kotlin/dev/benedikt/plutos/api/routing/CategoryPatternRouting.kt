package dev.benedikt.plutos.api.routing

import dev.benedikt.plutos.api.structure.*
import dev.benedikt.plutos.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.accountRouting() {
    route("/accounts") {
        // Get all accounts.
        get {
            val entities = transaction { Accounts.selectAll().map(ResultRow::toAccount) }
            call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
        }

        route("{accountId}") {
            // Get account.
            get {
                val id = call.parameters.getOrFail("accountId").toIntOrNull()
                val entity = transaction { id?.let { Accounts.select { Accounts.id eq it }.firstOrNull() }?.toAccount() }

                if (entity == null) {
                    call.respondError(HttpStatusCode.NotFound, ErrorObject("account_not_found", "The account does not exist."))
                    return@get
                }

                call.respondDocument(HttpStatusCode.OK, entity.toResourceObject())
            }

            // Delete account.
            delete {
                val id = call.parameters.getOrFail("accountId").toIntOrNull()
                if (id == null) {
                    call.respondDocument(HttpStatusCode.NotFound)
                    return@delete
                }

                val success = transaction { Accounts.deleteWhere { Accounts.id eq id } > 0 }
                if (!success) {
                    call.respondError(HttpStatusCode.NotFound, ErrorObject("account_not_found", "The account does not exist."))
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
