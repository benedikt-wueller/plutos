package dev.benedikt.plutos.models

import dev.benedikt.plutos.api.structure.Resource
import dev.benedikt.plutos.api.structure.ResourceObject
import dev.benedikt.plutos.api.structure.ResourceObjectBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@Serializable
data class Tag(val name: String, val color: String, val textColor: String, val description: String) : Resource {
    companion object { const val type = "tags" }

    @Transient var categoryId: Int? = null
}

object Tags : IntIdTable() {
    val name = varchar("name", 128)
    val color = varchar("color", 7)
    val textColor = varchar("text_color", 7)
    val categoryId = reference("category", Categories).nullable()
    val description = text("description").nullable()
}

fun ResultRow.toTag(): Model<Tag> {
    val tag = Tag(
        name = this[Tags.name],
        color = this[Tags.color],
        textColor = this[Tags.textColor],
        description = this[Tags.description] ?: ""
    )

    tag.categoryId = this[Tags.categoryId]?.value

    return Model(
        id = this[Tags.id].value,
        type = Tag.type,
        attributes = tag
    )
}

fun Tags.insert(tag: Model<Tag>) : Model<Tag> {
    val id = this.insertAndGetId {
        it[name] = tag.attributes.name
        it[color] = tag.attributes.color
        it[textColor] = tag.attributes.textColor
        it[categoryId] = tag.attributes.categoryId
        it[description] = tag.attributes.description
    }
    return tag.copy(id = id.value)
}

fun Tags.update(tag: Model<Tag>) : Boolean {
    return this.update({ Tags.id eq tag.id }) {
        it[name] = tag.attributes.name
        it[color] = tag.attributes.color
        it[textColor] = tag.attributes.textColor
        it[categoryId] = tag.attributes.categoryId
        it[description] = tag.attributes.description
    } > 0
}

fun Model<Tag>.toResourceObject(): ResourceObject {
    val entity = this
    return transaction {
        val patternIds = TagPatterns.slice(TagPatterns.id).select { TagPatterns.tagId eq entity.id }.map { it[TagPatterns.id].value }
        val statementIds = StatementTags.slice(StatementTags.statementId).select { StatementTags.tagId eq entity.id }.map { it[StatementTags.statementId].value }
        return@transaction ResourceObjectBuilder(entity, Tag::class)
            .relationship("patterns", TagPattern.type, patternIds)
            .relationship("statements", Statement.type, statementIds)
            .relationship("category", Category.type, entity.attributes.categoryId)
            .build()
    }
}
