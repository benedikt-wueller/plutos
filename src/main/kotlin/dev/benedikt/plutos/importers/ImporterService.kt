package dev.benedikt.plutos.importers

import dev.benedikt.plutos.importers.statements.CommerzbankCreditCardImporter
import dev.benedikt.plutos.importers.statements.CommerzbankImporter
import dev.benedikt.plutos.importers.statements.SparkasseImporter

object ImporterService {

    private val importers = mutableMapOf<String, Importer>()

    init {
        register(SparkasseImporter())
        register(CommerzbankImporter())
        register(CommerzbankCreditCardImporter())
    }

    private fun register(importer: Importer) {
        importers[importer.key] = importer
    }

    fun all(): List<Importer> = importers.values.toList()

    fun find(key: String) = importers[key]

}
