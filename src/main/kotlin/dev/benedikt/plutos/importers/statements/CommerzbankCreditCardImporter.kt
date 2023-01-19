package dev.benedikt.plutos.importers.statements

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.benedikt.plutos.helpers.toDoubleInternational
import dev.benedikt.plutos.models.Statement
import kotlinx.serialization.Serializable
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
class CommerzbankCreditCardImporter : StatementImporter("Commerzbank Credit Card Export (CSV)", "commerzbank_cc_csv", "text/csv") {

    override fun readStatements(inputStream: InputStream, parameters: Map<String, String>): List<ImportStatement> {
        val rows = csvReader { delimiter = ';' }.readAll(inputStream)
        return rows.drop(1).map {
            val valueDate = LocalDate.parse(it[0].trim(), dateTimeFormatter)
            val bookingDate = if (it[1].trim().any()) LocalDate.parse(it[1].trim(), dateTimeFormatter) else valueDate

            ImportStatement(
                Statement(
                    bookingDate.format(DateTimeFormatter.ISO_DATE),
                    valueDate.format(DateTimeFormatter.ISO_DATE),
                    "Credit Card",
                    it[3].trim().toDoubleInternational()!!,
                    it[4].trim().substring(0, 3).uppercase(),
                    it[2].trim(),
                    null,
                    null,
                    null,
                    null,
                    it[2].trim(),
                    null,
                    null
                ),
                it[7].trim()
            )
        }
    }

}
