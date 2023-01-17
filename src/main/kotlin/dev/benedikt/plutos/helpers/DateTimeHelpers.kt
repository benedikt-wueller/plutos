package dev.benedikt.plutos.helpers

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun parseLocalDate(value: String, vararg formatters: DateTimeFormatter) : LocalDate {
    return formatters.firstNotNullOf {
        try {
            LocalDate.parse(value, it)
        } catch (ignored: Exception) {
            null
        }
    }
}
