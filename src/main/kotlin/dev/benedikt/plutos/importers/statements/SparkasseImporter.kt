package dev.benedikt.plutos.importers.statements

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.benedikt.plutos.models.Statement
import dev.benedikt.plutos.helpers.condensedAnyOrNull
import dev.benedikt.plutos.helpers.parseLocalDate
import dev.benedikt.plutos.helpers.toDoubleInternational
import kotlinx.serialization.Serializable
import java.io.InputStream
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

private val dateTimeFormatterV2 = DateTimeFormatterBuilder()
    .appendPattern("dd.MM.yy")
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_DAY, 0)
    .parseDefaulting(ChronoField.SECOND_OF_DAY, 0)
    .toFormatter()

private val dateTimeFormatterV8 = DateTimeFormatterBuilder()
    .appendPattern("dd.MM.yyyy")
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_DAY, 0)
    .parseDefaulting(ChronoField.SECOND_OF_DAY, 0)
    .toFormatter()

@Serializable
class SparkasseImporter : StatementImporter("sparkasse_csv_camt", "Sparkasse Export (CSV-CAMT V2/V8)", "text/csv") {

    override fun readStatements(inputStream: InputStream): List<ImportStatement> {
        val rows = csvReader { this.delimiter = ';' }.readAll(inputStream)
        return rows.drop(1).map {
            ImportStatement(
                Statement(
                    parseLocalDate(it[1].trim(), dateTimeFormatterV2, dateTimeFormatterV8).format(DateTimeFormatter.ISO_DATE),
                    parseLocalDate(it[2].trim(), dateTimeFormatterV2, dateTimeFormatterV8).format(DateTimeFormatter.ISO_DATE),
                    it[3].trim(),
                    it[14].toDoubleInternational()!!,
                    it[15].trim().take(3).uppercase(),
                    it[4].condensedAnyOrNull(),
                    it[5].condensedAnyOrNull(),
                    it[6].condensedAnyOrNull(),
                    it[7].condensedAnyOrNull(),
                    it[8].condensedAnyOrNull(),
                    it[11].condensedAnyOrNull(),
                    it[12].condensedAnyOrNull(),
                    it[13].condensedAnyOrNull()
                ),
                it[0].trim()
            )
        }
    }

}
