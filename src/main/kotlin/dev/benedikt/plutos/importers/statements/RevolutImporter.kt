package dev.benedikt.plutos.importers.statements

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.benedikt.plutos.helpers.toDoubleInternational
import dev.benedikt.plutos.importers.ParameterDefinition
import dev.benedikt.plutos.models.Accounts
import dev.benedikt.plutos.models.Statement
import dev.benedikt.plutos.models.toAccount
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
class RevolutImporter : StatementImporter(
    "Revolut Export (CSV)", "revolut_csv", "text/csv",
    listOf(ParameterDefinition("account", "account", "Account"))
) {
    override fun readStatements(inputStream: InputStream, parameters: Map<String, String>): List<ImportStatement> {
        val account = transaction { Accounts.select { Accounts.id eq parameters["account"]!!.toInt() }.single().toAccount() }

        val rows = csvReader { delimiter = ',' }.readAll(inputStream)
        return rows.drop(1).map {
            val valueDate = LocalDate.parse(it[3].trim().replaceFirst(" ", "T"), DateTimeFormatter.ISO_DATE_TIME)
            val bookingDate = if (it[2].trim().any()) LocalDate.parse(it[2].trim().replaceFirst(" ", "T"), DateTimeFormatter.ISO_DATE_TIME) else valueDate

            ImportStatement(
                Statement(
                    bookingDate.format(DateTimeFormatter.ISO_DATE),
                    valueDate.format(DateTimeFormatter.ISO_DATE),
                    it[0].trim(),
                    it[5].toDoubleInternational()!!,
                    it[7].trim().take(3).uppercase(),
                    it[4].trim() + " | ${it[2]} / ${it[3]}",
                    null,
                    null,
                    null,
                    null,
                    it[1].trim(),
                    null,
                    null
                ),
                account.attributes.identifier
            )
        }
    }
}
