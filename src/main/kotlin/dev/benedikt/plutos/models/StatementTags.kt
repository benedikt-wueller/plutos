package dev.benedikt.plutos.models

import org.jetbrains.exposed.dao.id.IntIdTable

object StatementTags : IntIdTable() {
    val statementId = reference("statement", Statements)
    val tagId = reference("tag", Tags)
    val manual = bool("manual").default(false)

    init {
        uniqueIndex(statementId, tagId)
    }
}
