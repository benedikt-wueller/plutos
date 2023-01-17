package dev.benedikt.plutos.api.structure

import kotlinx.serialization.Serializable

@Serializable
data class ErrorObject(val code: String, val title: String? = null)
