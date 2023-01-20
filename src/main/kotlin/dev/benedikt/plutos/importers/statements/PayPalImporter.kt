package dev.benedikt.plutos.importers.statements

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.benedikt.plutos.helpers.condensedAnyOrNull
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
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

private val dateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("dd.MM.yyyy")
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_DAY, 0)
    .parseDefaulting(ChronoField.SECOND_OF_DAY, 0)
    .toFormatter()

@Serializable
class PayPalImporter : StatementImporter(
    "PayPal (CSV)", "paypal_csv", "text/csv",
    listOf(ParameterDefinition("account", "account", "Account"))
) {
    override fun readStatements(inputStream: InputStream, parameters: Map<String, String>): List<ImportStatement> {
        val account = transaction { Accounts.select { Accounts.id eq parameters["account"]!!.toInt() }.single().toAccount() }

        val rows = csvReader { delimiter = ',' }.readAll(inputStream)
        val statements = rows.drop(1).map {
            val date = LocalDate.parse(it[0], dateTimeFormatter)

            var purpose = "Transaction: ${it[9]}"
            it[16].condensedAnyOrNull()?.run { purpose += " - Invoice: $this" }
            it[17].condensedAnyOrNull()?.run { purpose += " - Related Transaction: $this" }
            it[10].condensedAnyOrNull()?.run { purpose += " - Email: $this" }

            Statement(
                date.format(DateTimeFormatter.ISO_DATE),
                date.format(DateTimeFormatter.ISO_DATE),
                it[3],
                it[5].toDoubleInternational() ?: 0.0,
                it[4].trim().take(3).uppercase(),
                purpose,
                null,
                null,
                null,
                null,
                it[11].condensedAnyOrNull() ?: it[12],
                it[13],
                null
            )
        }.toMutableList()

        val exchangeStatements = statements
            .filter { it.type == "Allgemeine Währungsumrechnung" || it.type == "General Currency Conversion" }
            .filter { it.amount < 0 }

        val relatedTransactionRegex = Regex("Related Transaction: (\\S+)")
        exchangeStatements.forEach { exchangeStatement ->
            val transactionId = relatedTransactionRegex.find(exchangeStatement.purpose!!)!!.groups[1]!!.value

            val actualStatement = statements.single { it.purpose!!.startsWith("Transaction: $transactionId") }

            val otherExchangeStatement = statements
                .filter { it.type == "Allgemeine Währungsumrechnung" || it.type == "General Currency Conversion" }
                .filter { it.purpose!!.contains("Related Transaction: $transactionId") }
                .filter { it.amount > 0 }
                .single { it != exchangeStatement }

            val newStatement = actualStatement.copy(
                amount = exchangeStatement.amount,
                currency = exchangeStatement.currency,
                purpose = "${actualStatement.purpose} - Exchanged: ${actualStatement.amount} ${actualStatement.currency}"
            )

            statements.remove(exchangeStatement)
            statements.remove(otherExchangeStatement)

            val index = statements.indexOf(actualStatement)
            statements[index] = newStatement
        }

        return statements.map {
            ImportStatement(it, account.attributes.identifier)
        }
    }
}
