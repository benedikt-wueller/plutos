package dev.benedikt.plutos.api.routing

import dev.benedikt.plutos.api.structure.respondDocument
import dev.benedikt.plutos.models.StatementLinks
import dev.benedikt.plutos.models.toResourceObject
import dev.benedikt.plutos.models.toStatementLink
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.statementLinkRouting() {
    route("statementLinks") {
        get {
            val entities = transaction { StatementLinks.selectAll().map(ResultRow::toStatementLink) }
            call.respondDocument(HttpStatusCode.OK, entities.map { it.toResourceObject() })
        }

        // TODO: allow adding and removing links
    }
}
