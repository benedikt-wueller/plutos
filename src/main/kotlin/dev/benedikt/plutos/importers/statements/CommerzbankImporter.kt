package dev.benedikt.plutos.importers.statements

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.benedikt.plutos.helpers.condensed
import dev.benedikt.plutos.helpers.parseLocalDate
import dev.benedikt.plutos.helpers.toDoubleInternational
import dev.benedikt.plutos.models.Statement
import kotlinx.serialization.Serializable
import java.io.InputStream
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
class CommerzbankImporter : StatementImporter("Commerzbank Export (CSV)", "commerzbank_csv", "text/csv") {

    override fun readStatements(inputStream: InputStream): List<ImportStatement> {
        val rows = csvReader { delimiter = ';' }.readAll(inputStream)
        return rows.drop(1).map {
            val body = it[3].trim()

            var purpose = body.split(Regex("\\sEnd-to-End-Ref\\.:")).first()
            val metaDataBody = body.substring(purpose.length)

            val metaData = mapOf(*Regex("\\s(\\S+):\\s(.*?)(?=\\s\\S+:|\\sSEPA|\$)")
                .findAll(metaDataBody)
                .map { match ->
                    purpose = purpose.replace(match.value, "")
                    return@map match.groups[1]!!.value to match.groups[2]!!.value
                }
                .toList()
                .toTypedArray()
            )

            val creditorId = metaData["Gläubiger-ID"]?.trim()
            val mandateReference = metaData["Mandatsref"]?.trim()
            val customerReference = (metaData["Kundenreferenz"] ?: metaData["End-to-End-Ref."])?.trim()

            var thirdPartyName: String? = null
            var thirdPartyAccount: String? = null
            var thirdPartyBankCode: String? = null

            val transferMatch = Regex("^(.*?)\\s([0-9A-Z]{8,11})\\s([0-9A-Z]{15,34})\\s").find(purpose)
            if (transferMatch != null) {
                purpose = purpose.replace(transferMatch.value, "")
                thirdPartyName = transferMatch.groups[1]!!.value.trim()
                thirdPartyAccount = transferMatch.groups[3]!!.value.trim()
                thirdPartyBankCode = transferMatch.groups[2]!!.value.trim()
            }

            ImportStatement(
                Statement(
                    parseLocalDate(it[0].trim(), dateTimeFormatter).format(DateTimeFormatter.ISO_DATE),
                    parseLocalDate(it[1].trim(), dateTimeFormatter).format(DateTimeFormatter.ISO_DATE),
                    it[2].trim(),
                    it[4].toDoubleInternational()!!,
                    it[5].trim().take(3).uppercase(),
                    purpose.condensed(),
                    creditorId,
                    mandateReference,
                    customerReference,
                    null,
                    thirdPartyName,
                    thirdPartyAccount,
                    thirdPartyBankCode
                ),
                it[8].trim()
            )
        }
    }

}
