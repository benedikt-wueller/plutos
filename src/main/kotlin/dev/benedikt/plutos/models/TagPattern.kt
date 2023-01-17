package dev.benedikt.plutos.models

import dev.benedikt.plutos.api.structure.ResourceObject
import dev.benedikt.plutos.api.structure.ResourceObjectBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
class TagPattern : Pattern {
    companion object { const val type = "tagPatterns" }

    @Transient
    var tagId: Int = -1
    @Transient
    var patternId: Int = -1

    constructor(name: String, regex: String, matchMode: MatchMode) : super(name, regex, matchMode)
}

object TagPatterns : IntIdTable() {
    val patternId = reference("pattern", Patterns).uniqueIndex()
    val tagId = reference("tag", Tags)
}

fun ResultRow.toTagPattern() = Model(
    id = this[TagPatterns.id].value,
    type = TagPattern.type,
    attributes = TagPattern(
        name = this[Patterns.name],
        regex = this[Patterns.regex],
        matchMode = this[Patterns.matchMode]
    ).also {
        it.patternId = this[Patterns.id].value
        it.tagId = this[TagPatterns.tagId].value
    }
)

fun Model<Pattern>.toTagPattern(patternId: Int, tagId: Int) = Model(
    id = this.id!!,
    type = TagPattern.type,
    attributes = TagPattern(
        name = this.attributes.name,
        regex = this.attributes.regex,
        matchMode = this.attributes.matchMode
    ).also {
        it.patternId = patternId
        it.tagId = tagId
    }
)

fun Patterns.insertTagPattern(pattern: Model<TagPattern>, tagId: Int) : Model<TagPattern> {
    val actualPattern = this.insertPattern(pattern)

    val id = TagPatterns.insertAndGetId {
        it[patternId] = actualPattern.id!!
        it[TagPatterns.tagId] = tagId
    }

    val attributes = TagPattern(
        pattern.attributes.name,
        pattern.attributes.regex,
        pattern.attributes.matchMode
    )

    attributes.tagId = tagId
    attributes.patternId = actualPattern.id!!

    return pattern.copy(id = id.value, attributes = attributes)
}

fun Model<TagPattern>.toResourceObject(): ResourceObject {
    val entity = this
    return transaction {
        return@transaction ResourceObjectBuilder(entity, TagPattern::class)
            .relationship("tag", Tag.type, entity.attributes.tagId)
            .build()
    }
}
