package dev.benedikt.plutos.importers.statements

import com.github.doyaaaaaken.kotlincsv.dsl.context.ExcessFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
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
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

private val dateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("dd.MM.yyyy")
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_DAY, 0)
    .parseDefaulting(ChronoField.SECOND_OF_DAY, 0)
    .toFormatter()

@Serializable
class AmazonLBBImporter : StatementImporter(
    "Amazon LBB (CSV)", "amazon_lbb_csv", "text/csv",
    listOf(ParameterDefinition("account", "account", "Account"))
) {

    override fun readStatements(inputStream: InputStream, parameters: Map<String, String>): List<ImportStatement> {
        val account = transaction { Accounts.select { Accounts.id eq parameters["account"]!!.toInt() }.single().toAccount() }

        return csvReader {
            delimiter = ';'
            excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.IGNORE
            insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE
        }.open(inputStream) {
            readNext() // skip first line
            val rows = readAllAsSequence()
            return@open rows.drop(1).map {
                val valueDate = LocalDate.parse(it[1].trim(), dateTimeFormatter)
                val bookingDate = if (it[2].trim().any()) LocalDate.parse(it[2].trim(), dateTimeFormatter) else valueDate

                ImportStatement(
                    Statement(
                        bookingDate.format(DateTimeFormatter.ISO_DATE),
                        valueDate.format(DateTimeFormatter.ISO_DATE),
                        it[4].trim(),
                        it[8].toDoubleInternational()!! * -1,
                        "EUR",
                        it[3].trim(),
                        null,
                        null,
                        null,
                        null,
                        it[3].trim(),
                        null,
                        null
                    ),
                    account.attributes.identifier
                )
            }.toList()
        }
    }

}
