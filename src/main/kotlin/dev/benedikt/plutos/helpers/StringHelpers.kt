package dev.benedikt.plutos

fun String.condensedAnyOrNull(): String? {
    val data = this.condensed()
    return if (data.any()) data else null
}

fun String.condensed(): String {
    return this.trim().replace(Regex("\\s\\s+"), " ")
}

fun String.toDoubleInternational(): Double? {
    return this.trim().replace(",", ".").toDoubleOrNull()
}
