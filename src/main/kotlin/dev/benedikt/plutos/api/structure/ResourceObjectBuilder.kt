package dev.benedikt.plutos.api.objects

object ResourcesBuilderSettings {
    var baseUrl = "http://localhost/api"
}

class ResourceObjectBuilder<T>(private val typePlural: String, private val type: String, private val id: Any, private val resource: T) {

    private val relationships = mutableMapOf<String, RelationshipObject>()

    fun relationship(name: String, type: String, id: Any?) {
        addRelationship(name, type, id?.let { ResourceIdentifierObject(type, it) })
    }

    fun relationship(name: String, type: String, ids: List<Any>) {
        val identifiers = ResourceIdentifierObjects()
        ids.forEach { identifiers.add(ResourceIdentifierObject(type, it)) }
        addRelationship(name, type, identifiers)
    }

    private fun addRelationship(name: String, type: String, linkage: ResourceLinkage?) {
        relationships[name] = RelationshipObject(
            ResourceLinks(related = "${ResourcesBuilderSettings.baseUrl}/${typePlural}/${id}/${name}"),
            linkage
        )
    }

    fun build() : ResourceObject<T> = ResourceObject(
        ResourceLinks(self = "${ResourcesBuilderSettings.baseUrl}/${typePlural}/${id}"),
        type,
        id,
        resource,
        relationships
    )

}
