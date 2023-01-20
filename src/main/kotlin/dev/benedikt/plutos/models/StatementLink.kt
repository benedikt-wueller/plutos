package dev.benedikt.plutos.models

import dev.benedikt.plutos.api.structure.Resource
import dev.benedikt.plutos.api.structure.ResourceObject
import dev.benedikt.plutos.api.structure.ResourceObjectBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
class StatementLink : Resource {

    companion object { const val type = "statementLinks" }

    @Transient var firstStatementId: Int? = null
    @Transient var secondStatementId: Int? = null

    constructor(firstStatementId: Int, secondStatementId: Int) {
        this.firstStatementId = firstStatementId
        this.secondStatementId = secondStatementId
    }

}

object StatementLinks : IntIdTable() {
    val firstStatementId = reference("first_statement", Statements)
    val secondStatementId = reference("second_statement", Statements)

    init {
        uniqueIndex(firstStatementId, secondStatementId)
    }
}

fun ResultRow.toStatementLink() : Model<StatementLink> {
    return Model(
        StatementLink.type,
        this[StatementLinks.id].value,
        attributes = StatementLink(
            this[StatementLinks.firstStatementId].value,
            this[StatementLinks.secondStatementId].value
        )
    )
}

fun Model<StatementLink>.toResourceObject() : ResourceObject {
    val entity = this
    return transaction {
        return@transaction ResourceObjectBuilder(entity, StatementLink::class)
            .relationship("firstStatement", Statement.type, entity.attributes.firstStatementId)
            .relationship("secondStatement", Statement.type, entity.attributes.secondStatementId)
            .build()
    }
}
