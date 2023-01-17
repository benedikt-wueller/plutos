package dev.benedikt.plutos.api.structure

import kotlinx.serialization.Serializable

@Serializable
data class RelationshipPatchBody(val data: List<ResourceIdentifierObject>?)
