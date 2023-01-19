package dev.benedikt.plutos.models

import dev.benedikt.plutos.api.structure.ResourceObject
import dev.benedikt.plutos.api.structure.ResourceObjectBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
class CategoryPattern : Pattern {
    companion object { const val type = "categoryPatterns" }

    @Transient var categoryId: Int = -1
    @Transient var patternId: Int = -1

    constructor(
        name: String,
        regex: String,
        matchMode: MatchMode,
        matchTargets: List<MatchTarget>,
        accountTargets: List<Int>,
        squishData: Boolean
    ) : super(name, regex, matchMode, matchTargets, accountTargets, squishData)
}

object CategoryPatterns : IntIdTable() {
    val patternId = reference("pattern", Patterns).uniqueIndex()
    val categoryId = reference("category", Categories)
}

fun ResultRow.toCategoryPattern() = Model(
    id = this[CategoryPatterns.id].value,
    type = CategoryPattern.type,
    attributes = CategoryPattern(
        name = this[Patterns.name],
        regex = this[Patterns.regex],
        matchMode = this[Patterns.matchMode],
        matchTargets = this[Patterns.matchTargets]?.split(",")?.map(MatchTarget::valueOf) ?: listOf(),
        accountTargets = this[Patterns.accountTargets]?.split(",")?.map(String::toInt) ?: listOf(),
        squishData = this[Patterns.squishData]
    ).also {
        it.patternId = this[Patterns.id].value
        it.categoryId = this[CategoryPatterns.categoryId].value
    }
)

fun Model<Pattern>.toCategoryPattern(patternId: Int, categoryId: Int): Model<CategoryPattern> {
    return Model(
        id = this.id!!,
        type = CategoryPattern.type,
        attributes = CategoryPattern(
            name = this.attributes.name,
            regex = this.attributes.regex,
            matchMode = this.attributes.matchMode,
            matchTargets = this.attributes.matchTargets,
            accountTargets = this.attributes.accountTargets,
            squishData = this.attributes.squishData
        ).also {
            it.patternId = patternId
            it.categoryId = categoryId
        }
    )
}

fun Patterns.insertCategoryPattern(pattern: Model<CategoryPattern>, categoryId: Int) : Model<CategoryPattern> {
    val actualPattern = this.insertPattern(pattern)

    val id = CategoryPatterns.insertAndGetId {
        it[patternId] = actualPattern.id!!
        it[CategoryPatterns.categoryId] = categoryId
    }

    val attributes = CategoryPattern(
        pattern.attributes.name,
        pattern.attributes.regex,
        pattern.attributes.matchMode,
        pattern.attributes.matchTargets,
        pattern.attributes.accountTargets,
        pattern.attributes.squishData
    )

    attributes.categoryId = categoryId
    attributes.patternId = actualPattern.id!!

    return pattern.copy(id = id.value, attributes = attributes)
}

fun Model<CategoryPattern>.toResourceObject(): ResourceObject {
    val entity = this
    return transaction {
        return@transaction ResourceObjectBuilder(entity, CategoryPattern::class)
            .relationship("category", Category.type, entity.attributes.categoryId)
            .build()
    }
}
