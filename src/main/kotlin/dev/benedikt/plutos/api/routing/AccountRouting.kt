package dev.benedikt.plutos.api.routing

import dev.benedikt.plutos.api.structure.ResourceObjectBuilder
import dev.benedikt.plutos.api.structure.respondDocument
import dev.benedikt.plutos.models.Accounts
import dev.benedikt.plutos.models.toAccount
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
        get {
            val accounts = transaction { Accounts.selectAll().map(ResultRow::toAccount) }

            call.respondDocument(HttpStatusCode.OK, accounts.map {
                ResourceObjectBuilder("accounts", it.id!!, it).build()
            })
        }

        get("{id}") {
            val id = call.parameters.getOrFail("id").toIntOrNull()
            val account = transaction { id?.let { Accounts.select { Accounts.id eq it }.firstOrNull() }?.toAccount() }

            call.respondDocument(if (account == null) HttpStatusCode.NotFound else HttpStatusCode.OK, account?.let {
                ResourceObjectBuilder("accounts", it.id!!, it).build()
            })
        }

        delete("{id}") {
            val id = call.parameters.getOrFail("id").toIntOrNull()
            if (id == null) {
                call.respondDocument(HttpStatusCode.NotFound)
                return@delete
            }

            transaction { Accounts.deleteWhere { Accounts.id eq id } }
            call.respondDocument(HttpStatusCode.OK)
        }
    }
}
