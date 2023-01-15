package dev.benedikt.plutos.api.structure

import kotlinx.serialization.Serializable

interface ResourceLinkage

@Serializable
data class RelationshipObject(val links: Links, val data: ResourceLinkage?)
