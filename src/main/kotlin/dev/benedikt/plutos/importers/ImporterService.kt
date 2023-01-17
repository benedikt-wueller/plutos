package dev.benedikt.plutos.importers

object ImporterService {

    private val importers = mutableMapOf<String, Importer>()

    init {
        register(SparkasseImporter())
    }

    private fun register(importer: Importer) {
        importers[importer.key] = importer
    }

    fun all() = importers.values.toList()

    fun find(key: String) = importers[key]

}
