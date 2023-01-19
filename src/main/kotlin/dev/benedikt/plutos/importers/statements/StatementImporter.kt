package dev.benedikt.plutos.importers.statements

import dev.benedikt.plutos.importers.Importer
import dev.benedikt.plutos.importers.ParameterDefinition
import dev.benedikt.plutos.models.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.InputStream

data class ImportStatement(val statement: Statement, val accountIdentifier: String)

@Serializable
abstract class StatementImporter : Importer {

    constructor(name: String, key: String, fileFormat: String, parameters: List<ParameterDefinition> = listOf()) : super(name, key, fileFormat, parameters)

    override fun import(inputStream: InputStream, parameters: Map<String, String>) {
        val statements = this.readStatements(inputStream, parameters)
        statements.forEach {
            it.statement.updateIdHash()
            it.statement.updateContentHash()
        }

        val idHashes = statements.map { it.statement.idHash!! }

        transaction {
            val existingStatements = Statements.select { Statements.idHash inList idHashes }.map(ResultRow::toStatement)

            val outdatedStatements = existingStatements.filter { statement ->
                val importStatement = statements.single { it.statement.idHash == statement.attributes.idHash }
                return@filter statement.attributes.contentHash != importStatement.statement.contentHash
            }

            val newStatements = statements.filter { importStatement -> !existingStatements.any { it.attributes.idHash == importStatement.statement.idHash } }

            val accounts = determineAccounts(statements.map { it.accountIdentifier }.distinct())

            outdatedStatements.forEach { statement ->
                val model = Model(
                    Statement.type, statement.id,
                    statements.single { it.statement.idHash == statement.attributes.idHash }.statement.copy(
                        comment = statement.attributes.comment,
                    ).also {
                        it.accountId = statement.attributes.accountId
                        it.categoryId = statement.attributes.categoryId
                        it.idHash = statement.attributes.idHash
                        it.contentHash = statement.attributes.contentHash
                    }
                )
                Statements.update(model)
            }

            applyCategoryAndTags(outdatedStatements)

            if (!newStatements.any()) return@transaction

            val tags = Tags.selectAll().map(ResultRow::toTag)
            val tagPatterns = TagPatterns.leftJoin(Patterns).selectAll().map(ResultRow::toTagPattern)

            val categoryPatterns = CategoryPatterns.leftJoin(Patterns).selectAll().map(ResultRow::toCategoryPattern)
            val defaultCategoryId = Categories.slice(Categories.id).select { Categories.default eq true }.first()[Categories.id].value

            newStatements.forEach { importStatement ->
                val model = Model(
                    Statement.type, null,
                    importStatement.statement.copy().also {
                        it.accountId = accounts[importStatement.accountIdentifier]
                        it.idHash = importStatement.statement.idHash
                        it.contentHash = importStatement.statement.contentHash
                    }
                )

                val result = determineCategoryAndTags(model, tags, tagPatterns, categoryPatterns, defaultCategoryId)
                model.attributes.categoryId = result.categoryId

                val newStatement = Statements.insert(model)

                result.tagIds.forEach { id ->
                    StatementTags.insert {
                        it[statementId] = newStatement.id!!
                        it[tagId] = id
                    }
                }
            }
        }
    }

    private fun determineAccounts(identifiers: Iterable<String>) : Map<String, Int> {
        val accounts = Accounts.select { Accounts.identifier inList identifiers }.map(ResultRow::toAccount)

        val newAccounts = identifiers
            .filter { identifier -> !accounts.any { it.attributes.identifier == identifier } }
            .map { Model(Account.type, null, Account(it, it, null)) }
            .map { Accounts.insert(it) }

        return mapOf(*accounts.union(newAccounts).map { it.attributes.identifier to it.id!! }.toTypedArray())
    }

    abstract fun readStatements(inputStream: InputStream, parameters: Map<String, String>): List<ImportStatement>

}
