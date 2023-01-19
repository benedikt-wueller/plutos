package dev.benedikt.plutos.models

import dev.benedikt.plutos.api.structure.Resource
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import kotlin.reflect.KProperty1

enum class MatchMode {
    PARTIAL_MATCH,
    FULL_MATCH,
    NO_PARTIAL_MATCH,
    NO_FULL_MATCH,
}

enum class MatchTarget(val property: KProperty1<Statement, *>) {
    PURPOSE(Statement::purpose),
    CREDITOR_ID(Statement::creditorId),
    MANDATE_REFERENCE(Statement::mandateReference),
    CUSTOMER_REFERENCE(Statement::customerReference),
    PAYMENT_INFORMATION_ID(Statement::paymentInformationId),
    THIRD_PARTY_NAME(Statement::thirdPartyName),
    THIRD_PARTY_ACCOUNT(Statement::thirdPartyAccount),
    THIRD_PARTY_BANK_CODE(Statement::thirdPartyBankCode),
    TYPE(Statement::type)
}

@Serializable
open class Pattern(
    val name: String,
    val regex: String,
    val matchMode: MatchMode,
    val matchTargets: List<MatchTarget>,
    val accountTargets: List<Int>,
    val squishData: Boolean,
) : Resource {
    companion object { const val type = "patterns" }
}

object Patterns : IntIdTable() {
    val name = varchar("name", 128)
    val regex = text("regex")
    val matchMode = enumeration<MatchMode>("match_mode")
    val matchTargets = text("match_targets").nullable().default(null)
    val squishData = bool("squish_data").default(false)
    val accountTargets = text("account_targets").nullable().default(null)
}

fun Patterns.insertPattern(pattern: Model<out Pattern>) : Model<out Pattern> {
    val id = this.insertAndGetId {
        it[name] = pattern.attributes.name
        it[regex] = pattern.attributes.regex
        it[matchMode] = pattern.attributes.matchMode
        it[matchTargets] = pattern.attributes.matchTargets.let { target -> if (target.any()) target else null }?.joinToString(",") { option -> option.toString() }
        it[squishData] = pattern.attributes.squishData
        it[accountTargets] = pattern.attributes.accountTargets.let { account -> if (account.any()) account else null }?.joinToString(",") { option -> option.toString() }
    }
    return pattern.copy(id = id.value)
}

fun Patterns.updatePattern(pattern: Model<out Pattern>) : Boolean {
    return this.update({ Patterns.id eq pattern.id }) {
        it[name] = pattern.attributes.name
        it[regex] = pattern.attributes.regex
        it[matchMode] = pattern.attributes.matchMode
        it[matchTargets] = pattern.attributes.matchTargets.let { target -> if (target.any()) target else null }?.joinToString(",") { option -> option.toString() }
        it[squishData] = pattern.attributes.squishData
        it[accountTargets] = pattern.attributes.accountTargets.let { account -> if (account.any()) account else null }?.joinToString(",") { option -> option.toString() }
    } > 0
}
