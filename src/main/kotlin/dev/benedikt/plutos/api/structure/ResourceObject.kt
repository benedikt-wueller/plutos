package dev.benedikt.plutos.api.objects

data class ResourceObject<T>(val links: ResourceLinks, val type: String, val id: Any, val attributes: T, val relationships: Map<String, RelationshipObject>)
