package dev.benedikt.plutos.importers

import kotlinx.serialization.Serializable
import java.io.InputStream

@Serializable
abstract class Importer(val key: String, val name: String, val fileFormat: String) {
    abstract fun import(inputStream: InputStream)
}
