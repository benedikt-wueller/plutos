package dev.benedikt.plutos.models

import dev.benedikt.plutos.api.structure.Resource
import dev.benedikt.plutos.api.structure.ResourceObject
import kotlinx.serialization.Serializable

@Serializable
data class Model<T : Resource>(val type: String, val id: Int?, val attributes: T)

@Serializable
data class ModelWrapper<T : Resource>(val data: Model<T>)
