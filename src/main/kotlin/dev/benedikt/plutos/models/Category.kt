package dev.benedikt.plutos.models

import dev.benedikt.plutos.api.structure.Resource
import dev.benedikt.plutos.api.structure.ResourceObject
import dev.benedikt.plutos.api.structure.ResourceObjectBuilder
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@Serializable
data class Category(val name: String, val color: String, val textColor: String, val limit: Double?, val default: Boolean) : Resource {
    companion object { const val type = "categories" }
}

object Categories : IntIdTable() {
    val name = varchar("name", 128)
    val color = varchar("color", 7)
    val textColor = varchar("text_color", 7)
    val limit = double("limit").nullable()
    val default = bool("default")
}

fun ResultRow.toCategory() = Model(
    id = this[Categories.id].value,
    type = Category.type,
    attributes = Category(
        name = this[Categories.name],
        color = this[Categories.color],
        textColor = this[Categories.textColor],
        limit = this[Categories.limit],
        default = this[Categories.default]
    )
)

fun Categories.insert(category: Model<Category>) : Model<Category> {
    val id = this.insertAndGetId {
        it[name] = category.attributes.name
        it[color] = category.attributes.color
        it[textColor] = category.attributes.textColor
        it[limit] = category.attributes.limit
        it[default] = category.attributes.default
    }
    return category.copy(id = id.value)
}

fun Categories.update(category: Model<Category>) : Boolean {
    return this.update({ Categories.id eq category.id }) {
        it[name] = category.attributes.name
        it[color] = category.attributes.color
        it[textColor] = category.attributes.textColor
        it[limit] = category.attributes.limit
        it[default] = category.attributes.default
    } > 0
}

fun Model<Category>.toResourceObject(): ResourceObject {
    val entity = this
    return transaction {
        val patternIds = CategoryPatterns.slice(CategoryPatterns.id).select { CategoryPatterns.categoryId eq entity.id }.map { it[CategoryPatterns.id].value }
        val statementIds = Statements.slice(Statements.id).select { Statements.categoryId eq entity.id }.map { it[Statements.id].value }
        val tagIds = Tags.slice(Tags.id).select { Tags.categoryId eq entity.id }.map { it[Tags.id].value }
        return@transaction ResourceObjectBuilder(entity, Category::class)
            .relationship("patterns", CategoryPattern.type, patternIds)
            .relationship("statements", Statement.type, statementIds)
            .relationship("tags", Tag.type, tagIds)
            .build()
    }
}
