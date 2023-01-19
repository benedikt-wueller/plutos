package dev.benedikt.plutos.importers

import dev.benedikt.plutos.importers.statements.*

object ImporterService {

    private val importers = mutableMapOf<String, Importer>()

    init {
        register(SparkasseImporter())
        register(CommerzbankImporter())
        register(CommerzbankCreditCardImporter())
        register(AmazonLBBImporter())
        register(RevolutImporter())
    }

    private fun register(importer: Importer) {
        importers[importer.key] = importer
    }

    fun all(): List<Importer> = importers.values.toList()

    fun find(key: String) = importers[key]

}
