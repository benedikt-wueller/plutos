package dev.benedikt.plutos.models

import dev.benedikt.plutos.api.structure.Resource
import dev.benedikt.plutos.api.structure.ResourceObject
import dev.benedikt.plutos.api.structure.ResourceObjectBuilder
import dev.benedikt.plutos.models.Categories.default
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class Statement(
    val bookingDate: String,
    val valueDate: String,
    val type: String,
    val amount: Double,
    val currency: String,
    val purpose: String?,
    val creditorId: String?,
    val mandateReference: String?,
    val customerReference: String?,
    val paymentInformationId: String?,
    val thirdPartyName: String?,
    val thirdPartyAccount: String?,
    val thirdPartyBankCode: String?,
    val comment: String? = null,
) : Resource {
    companion object { const val type = "statements" }

    @Transient var categoryId: Int? = null
    @Transient var accountId: Int? = null
    @Transient var idHash: String? = null
    @Transient var contentHash: String? = null
    @Transient var manualCategory: Boolean? = false

    val contentValues get() = arrayOf(
        bookingDate, valueDate, type, amount, currency, purpose, creditorId, mandateReference,
        customerReference, paymentInformationId, thirdPartyName, thirdPartyAccount, thirdPartyBankCode
    ).map { it.toString() }

    fun updateIdHash() {
        val data = arrayOf(
            valueDate, type, purpose, currency, amount, creditorId, mandateReference,
            customerReference, paymentInformationId, thirdPartyAccount, thirdPartyBankCode
        ).joinToString { it.toString() }.toByteArray()

        idHash = BigInteger(1, MessageDigest.getInstance("MD5").digest(data))
            .toString(16)
            .padStart(32, '0')
    }

    fun updateContentHash() {
        val data = arrayOf(bookingDate, thirdPartyName).joinToString { it.toString() }.toByteArray()

        contentHash = BigInteger(1, MessageDigest.getInstance("MD5").digest(data))
            .toString(16)
            .padStart(32, '0')
    }
}

object Statements : IntIdTable() {
    val accountId = reference("account", Accounts)
    val bookingDate = date("booking_date")
    val valueDate = date("value_date")
    val type = varchar("type", 64)
    val amount = double("amount")
    val currency = varchar("currency", 3)
    val purpose = text("purpose").nullable()
    val creditorId = varchar("creditor_id", 35).nullable()
    val mandateReference = varchar("mandate_reference", 35).nullable()
    val customerReference = varchar("customer_reference", 35).nullable()
    val paymentInformationId = varchar("payment_information_id", 35).nullable()
    val thirdPartyName = varchar("third_party_name", 200).nullable()
    val thirdPartyAccount = varchar("third_party_account", 34).nullable()
    val thirdPartyBankCode = varchar("third_party_bank_code", 11).nullable()
    val categoryId = reference("category", Categories).index()
    val comment = text("comment").nullable()
    val idHash = varchar("id_hash", 32).uniqueIndex()
    val contentHash = varchar("content_hash", 32)
    val manualCategory = bool("manual_category").default(false)
}

fun ResultRow.toStatement(): Model<Statement> {
    val statement = Statement(
        bookingDate = this[Statements.bookingDate].format(DateTimeFormatter.ISO_DATE),
        valueDate = this[Statements.valueDate].format(DateTimeFormatter.ISO_DATE),
        type = this[Statements.type],
        amount = this[Statements.amount],
        currency = this[Statements.currency],
        purpose = this[Statements.purpose],
        creditorId = this[Statements.creditorId],
        mandateReference = this[Statements.mandateReference],
        customerReference = this[Statements.customerReference],
        paymentInformationId = this[Statements.paymentInformationId],
        thirdPartyName = this[Statements.thirdPartyName],
        thirdPartyAccount = this[Statements.thirdPartyAccount],
        thirdPartyBankCode = this[Statements.thirdPartyBankCode],
        comment = this[Statements.comment]
    )

    statement.accountId = this[Statements.accountId].value
    statement.categoryId = this[Statements.categoryId].value
    statement.idHash = this[Statements.idHash]
    statement.contentHash = this[Statements.contentHash]
    statement.manualCategory = this[Statements.manualCategory]

    return Model(
        id = this[Statements.id].value,
        type = Statement.type,
        attributes = statement
    )
}

fun Statements.insert(entity: Model<Statement>) : Model<Statement> {
    val attributes = entity.attributes
    val id = this.insertAndGetId {
        it[accountId] = attributes.accountId!!
        it[bookingDate] = attributes.bookingDate.let { dt -> LocalDate.parse(dt, DateTimeFormatter.ISO_DATE) }
        it[valueDate] = attributes.valueDate.let { dt -> LocalDate.parse(dt, DateTimeFormatter.ISO_DATE) }
        it[type] = attributes.type
        it[amount] = attributes.amount
        it[currency] = attributes.currency
        it[purpose] = attributes.purpose
        it[creditorId] = attributes.creditorId
        it[mandateReference] = attributes.mandateReference
        it[customerReference] = attributes.customerReference
        it[paymentInformationId] = attributes.paymentInformationId
        it[thirdPartyName] = attributes.thirdPartyName
        it[thirdPartyAccount] = attributes.thirdPartyAccount
        it[thirdPartyBankCode] = attributes.thirdPartyBankCode
        it[categoryId] = attributes.categoryId!!
        it[comment] = attributes.comment
        it[idHash] = attributes.idHash!!
        it[contentHash] = attributes.contentHash!!
        it[manualCategory] = attributes.manualCategory!!
    }
    return entity.copy(id = id.value)
}

fun Statements.update(entity: Model<Statement>) : Boolean {
    val attributes = entity.attributes
    return this.update({ Statements.id eq entity.id }) {
        it[accountId] = attributes.accountId!!
        it[bookingDate] = attributes.bookingDate.let { dt -> LocalDate.parse(dt, DateTimeFormatter.ISO_DATE) }
        it[valueDate] = attributes.valueDate.let { dt -> LocalDate.parse(dt, DateTimeFormatter.ISO_DATE) }
        it[type] = attributes.type
        it[amount] = attributes.amount
        it[currency] = attributes.currency
        it[purpose] = attributes.purpose
        it[creditorId] = attributes.creditorId
        it[mandateReference] = attributes.mandateReference
        it[customerReference] = attributes.customerReference
        it[paymentInformationId] = attributes.paymentInformationId
        it[thirdPartyName] = attributes.thirdPartyName
        it[thirdPartyAccount] = attributes.thirdPartyAccount
        it[thirdPartyBankCode] = attributes.thirdPartyBankCode
        it[categoryId] = attributes.categoryId!!
        it[comment] = attributes.comment
        it[idHash] = attributes.idHash!!
        it[contentHash] = attributes.contentHash!!
        it[manualCategory] = attributes.manualCategory!!
    } > 0
}

fun Model<Statement>.toResourceObject(): ResourceObject {
    val entity = this
    return transaction {
        val tagIds = StatementTags.slice(StatementTags.tagId).select { StatementTags.statementId eq entity.id }.map { it[StatementTags.tagId].value }

        return@transaction ResourceObjectBuilder(entity, Statement::class)
            .relationship("category", Category.type, entity.attributes.categoryId)
            .relationship("account", Account.type, entity.attributes.accountId)
            .relationship("tags", Tag.type, tagIds)
            .build()
    }
}

data class CategoryAndTagResult(val categoryId: Int, val tagIds: List<Int>)

fun applyCategoryAndTags(statements: List<Model<Statement>>) {
    val tagPatterns = TagPatterns.leftJoin(Patterns).selectAll().map(ResultRow::toTagPattern)

    val categoryPatterns = CategoryPatterns.leftJoin(Patterns).selectAll().map(ResultRow::toCategoryPattern)
    val defaultCategoryId = Categories.slice(Categories.id).select { default eq true }.first()[Categories.id].value

    val categoryIds = Categories.slice(Categories.id).selectAll().map { it[Categories.id].value }

    StatementTags.deleteWhere { manual eq false }
    statements.forEach { statement ->
        val tagIds = determineTagIds(statement.attributes, tagPatterns)
        val manualTagIds = StatementTags.slice(StatementTags.id).select { StatementTags.statementId eq statement.id }.map { it[StatementTags.id].value }

        val newTagIds = tagIds.filter { !manualTagIds.contains(it) }
        newTagIds.forEach { id ->
            StatementTags.insert {
                it[tagId] = id
                it[statementId] = statement.id!!
            }
        }

        if (statement.attributes.manualCategory == true && categoryIds.contains(statement.attributes.categoryId)) return@forEach

        val allTagIds = tagIds.union(manualTagIds).distinct()
        val categoryId = determineCategoryId(statement.attributes, categoryPatterns, defaultCategoryId, allTagIds)
        Statements.update({ Statements.id eq statement.id }) { it[Statements.categoryId] = categoryId }
    }
}

fun determineCategoryAndTags(
    statement: Model<Statement>,
    tags: List<Model<Tag>>,
    tagPatterns: List<Model<TagPattern>>,
    categoryPatterns: List<Model<CategoryPattern>>,
    defaultCategoryId: Int
) : CategoryAndTagResult {
    val tagIds = determineTagIds(statement.attributes, tagPatterns)

    val preferredCategoryIds = tags
        .filter { tagIds.contains(it.id) }
        .mapNotNull { it.attributes.categoryId }

    val categoryId = determineCategoryId(statement.attributes, categoryPatterns, defaultCategoryId, preferredCategoryIds)
    return CategoryAndTagResult(categoryId, tagIds)
}

fun determineCategoryId(statement: Statement, patterns: List<Model<CategoryPattern>>, defaultCategoryId: Int, preferredCategoryIds: List<Int> = listOf()) : Int {
    val excludedCategories = mutableSetOf<Int>()
    val matches = mutableMapOf<Int, Int>()

    patterns.forEach { pattern ->
        val regex = Regex(pattern.attributes.regex)
        val matching = statement.contentValues.any {
            when (pattern.attributes.matchMode) {
                MatchMode.PARTIAL_MATCH, MatchMode.NO_PARTIAL_MATCH -> regex.containsMatchIn(it)
                MatchMode.FULL_MATCH, MatchMode.NO_FULL_MATCH -> regex.matches(it)
            }
        }

        if (!matching) return@forEach
        if (pattern.attributes.matchMode == MatchMode.NO_PARTIAL_MATCH || pattern.attributes.matchMode == MatchMode.NO_FULL_MATCH) {
            excludedCategories.add(pattern.attributes.categoryId)
        } else {
            matches[pattern.attributes.categoryId] = matches.getOrPut(pattern.attributes.categoryId) { 0 } + 1
        }
    }

    val validMatches = matches.filter { !excludedCategories.contains(it.key) }.toMutableMap()
    val maxAmount = validMatches.values.maxOrNull() ?: 0

    preferredCategoryIds
        .filter { !excludedCategories.contains(it) }
        .forEach { validMatches[it] = (validMatches[it] ?: 0) + maxAmount + 1 }

    return validMatches.maxByOrNull { it.value }?.key ?: defaultCategoryId
}

fun determineTagIds(statement: Statement, patterns: List<Model<TagPattern>>) : List<Int> {
    val excludedIds = mutableSetOf<Int>()
    val matchedIds = mutableSetOf<Int>()

    patterns.forEach { pattern ->
        val regex = Regex(pattern.attributes.regex)
        val matching = statement.contentValues.any {
            when (pattern.attributes.matchMode) {
                MatchMode.PARTIAL_MATCH, MatchMode.NO_PARTIAL_MATCH -> regex.containsMatchIn(it)
                MatchMode.FULL_MATCH, MatchMode.NO_FULL_MATCH -> regex.matches(it)
            }
        }

        if (!matching) return@forEach
        if (pattern.attributes.matchMode == MatchMode.NO_PARTIAL_MATCH || pattern.attributes.matchMode == MatchMode.NO_FULL_MATCH) {
            excludedIds.add(pattern.attributes.tagId)
        } else {
            matchedIds.add(pattern.attributes.tagId)
        }
    }

    return matchedIds.filter { !excludedIds.contains(it) }
}
