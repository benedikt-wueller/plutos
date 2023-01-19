package dev.benedikt.plutos.importers

import kotlinx.serialization.Serializable
import java.io.InputStream

@Serializable
data class ParameterDefinition(val key: String, val type: String, val name: String)

@Serializable
abstract class Importer(val name: String, val key: String, val fileFormat: String, val parameters: List<ParameterDefinition>) {
    abstract fun import(inputStream: InputStream, parameters: Map<String, String>)
}
