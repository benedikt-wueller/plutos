package dev.benedikt.plutos.importers

import kotlinx.serialization.Serializable
import java.io.InputStream

@Serializable
abstract class Importer(val name: String, val key: String, val fileFormat: String) {
    abstract fun import(inputStream: InputStream)
}
