package dev.benedikt.plutos.api.structure

import kotlinx.serialization.Serializable

@Serializable
data class ResourceIdentifierObject(val type: String, val id: Int)
