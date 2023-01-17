package dev.benedikt.plutos.models

import dev.benedikt.plutos.api.structure.Resource
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update

enum class MatchMode {
    PARTIAL_MATCH,
    FULL_MATCH,
    NO_PARTIAL_MATCH,
    NO_FULL_MATCH
}

@Serializable
open class Pattern(val name: String, val regex: String, val matchMode: MatchMode) : Resource {
    companion object { const val type = "patterns" }
}

object Patterns : IntIdTable() {
    val name = varchar("name", 128)
    val regex = text("regex")
    val matchMode = enumeration<MatchMode>("match_mode")
}

fun ResultRow.toPattern() = Model(
    id = this[Patterns.id].value,
    type = Pattern.type,
    attributes = Pattern(
        name = this[Patterns.name],
        regex = this[Patterns.regex],
        matchMode = this[Patterns.matchMode]
    )
)

fun Patterns.insertPattern(pattern: Model<out Pattern>) : Model<out Pattern> {
    val id = this.insertAndGetId {
        it[name] = pattern.attributes.name
        it[regex] = pattern.attributes.regex
        it[matchMode] = pattern.attributes.matchMode
    }
    return pattern.copy(id = id.value)
}

fun Patterns.updatePattern(pattern: Model<out Pattern>) : Boolean {
    return this.update({ Patterns.id eq pattern.id }) {
        it[name] = pattern.attributes.name
        it[regex] = pattern.attributes.regex
        it[matchMode] = pattern.attributes.matchMode
    } > 0
}
