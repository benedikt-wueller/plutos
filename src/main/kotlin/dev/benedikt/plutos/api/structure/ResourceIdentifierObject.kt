package dev.benedikt.plutos.api.objects

data class ResourceIdentifierObject(val type: String, val id: Any) : ResourceLinkage

class ResourceIdentifierObjects : ArrayList<ResourceIdentifierObject>(), ResourceLinkage
