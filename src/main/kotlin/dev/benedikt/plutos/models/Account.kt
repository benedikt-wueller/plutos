package dev.benedikt.plutos.models

import dev.benedikt.plutos.api.structure.Resource
import dev.benedikt.plutos.api.structure.ResourceObject
import dev.benedikt.plutos.api.structure.ResourceObjectBuilder
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@Serializable
data class Account(val name: String, val identifier: String, val currency: String?) : Resource {
    companion object { const val type = "accounts" }
}

object Accounts : IntIdTable() {
    val name = varchar("name", 128).uniqueIndex()
    val identifier = varchar("identifier", 64).uniqueIndex()
    val currency = varchar("currency", 3).nullable()
}

fun ResultRow.toAccount() = Model(
    id = this[Accounts.id].value,
    type = Account.type,
    attributes = Account(
        name = this[Accounts.name],
        identifier = this[Accounts.identifier],
        currency = this[Accounts.currency]
    )
)

fun Accounts.insert(account: Model<Account>) : Model<Account> {
    val id = this.insertAndGetId {
        it[name] = account.attributes.name
        it[identifier] = account.attributes.identifier
        it[currency] = account.attributes.currency
    }
    return account.copy(id = id.value)
}

fun Accounts.update(account: Model<Account>) : Boolean {
    return this.update({ Accounts.id eq account.id }) {
        it[name] = account.attributes.name
        it[identifier] = account.attributes.identifier
        it[currency] = account.attributes.currency
    } > 0
}

fun Model<Account>.toResourceObject(): ResourceObject {
    val entity = this
    return transaction {
        val statementIds = Statements.slice(Statements.id).select { Statements.accountId eq entity.id }.map { it[Statements.id].value }
        ResourceObjectBuilder(entity, Account::class)
            .relationship("statements", Statement.type, statementIds)
            .build()
    }
}

