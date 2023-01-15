package dev.benedikt.plutos.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update

data class Category(val id: Int?, val name: String, val color: String)

object Categories : IntIdTable() {
    val name = varchar("name", 128)
    val color = varchar("color", 6)
}

fun ResultRow.toCategory() = Category(
    id = this[Categories.id].value,
    name = this[Categories.name],
    color = this[Categories.color]
)

fun Categories.insert(category: Category) : Category {
    val id = this.insertAndGetId {
        it[name] = category.name
        it[color] = category.color
    }
    return category.copy(id = id.value)
}

fun Categories.update(category: Category) {
    this.update({ Accounts.id eq category.id }) {
        it[name] = category.name
        it[color] = category.color
    }
}
