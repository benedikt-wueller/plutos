package dev.benedikt.plutos.old.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object DatabaseService {

    init {
        Database.connect("jdbc:sqlite:plutos.db", driver = "org.sqlite.JDBC")
    }

    fun setup() = transaction {
        SchemaUtils.createMissingTablesAndColumns(Accounts, Categories, Transactions)
        createDefaultCategories()
    }

    private fun createDefaultCategories() {
        if (CategoryPatterns.exists()) {
            SchemaUtils.createMissingTablesAndColumns(CategoryPatterns)
            return
        }

        SchemaUtils.create(CategoryPatterns)

        Categories.insert {
            it[Categories.id] = UUID.fromString("00000000-0000-0000-0000-000000000000")
            it[name] = "Other"
            it[budget] = null
        }
    }

}
