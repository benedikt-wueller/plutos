package dev.benedikt.plutos

import dev.benedikt.plutos.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object DatabaseService {

    init {
        Database.connect("jdbc:sqlite:plutos.db", driver = "org.sqlite.JDBC")
    }

    fun setup() = transaction {
        SchemaUtils.createMissingTablesAndColumns(
            Patterns,
            Categories,
            CategoryPatterns,
            Tags,
            TagPatterns,
            Accounts,
            Statements,
            StatementTags
        )

        createDefaultCategories()
    }

    private fun createDefaultCategories() {
        val exists = Categories.select { Categories.default eq true }.any()
        if (exists) return
        Categories.insert(Model(Category.type, null, Category("Other", "#E5E7EB", "#000000", null, true, "")))
    }

}
