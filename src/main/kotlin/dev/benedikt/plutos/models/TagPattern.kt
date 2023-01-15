package dev.benedikt.plutos.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update

data class CategoryPattern(val id: Int?, val patternId: Int, val categoryId: Int)

object CategoryPatterns : IntIdTable() {
    val patternId = reference("pattern", Patterns, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val categoryId = reference("category", Categories)
}

fun ResultRow.toCategoryPattern() = CategoryPattern(
    id = this[CategoryPatterns.id].value,
    patternId = this[CategoryPatterns.patternId].value,
    categoryId = this[CategoryPatterns.categoryId].value
)

fun CategoryPatterns.insert(pattern: CategoryPattern) : CategoryPattern {
    val id = this.insertAndGetId {
        it[patternId] = pattern.patternId
        it[categoryId] = pattern.categoryId
    }
    return pattern.copy(id = id.value)
}

fun CategoryPatterns.update(pattern: CategoryPattern) {
    this.update({ CategoryPatterns.id eq pattern.id }) {
        it[patternId] = pattern.patternId
        it[categoryId] = pattern.categoryId
    }
}
