package dev.benedikt.plutos.api.structure

import dev.benedikt.plutos.models.*
import kotlin.reflect.KClass

object ResourceBuilderSettings {
    var baseUrl = "http://localhost/api"
}

class ResourceObjectBuilder<T : Resource>(private val resource: Model<T>, private val type: KClass<T>) {

    private val relationships = mutableMapOf<String, RelationshipObject>()

    fun relationship(name: String, type: String, id: Int?) : ResourceObjectBuilder<T> {
        relationships[name] = SingleRelationshipObject(
            Links(related = "${ResourceBuilderSettings.baseUrl}/${resource.type}/${resource.id}/${name}"),
            id?.let { ResourceIdentifierObject(type, it) }
        )
        return this
    }

    fun relationship(name: String, type: String, ids: List<Int>) : ResourceObjectBuilder<T> {
        val identifiers = mutableListOf<ResourceIdentifierObject>()
        ids.forEach { identifiers.add(ResourceIdentifierObject(type, it)) }
        relationships[name] = MultiRelationshipObject(
            Links(related = "${ResourceBuilderSettings.baseUrl}/${resource.type}/${resource.id}/${name}"),
            identifiers
        )
        return this
    }

    fun build() : ResourceObject {
        val links = Links(self = "${ResourceBuilderSettings.baseUrl}/${resource.type}/${resource.id}")
        return when (type) {
            Account::class -> AccountResourceObject(resource.id, resource.attributes as Account, relationships, links)
            Statement::class -> StatementResourceObject(resource.id, resource.attributes as Statement, relationships, links)
            Category::class -> CategoryResourceObject(resource.id, resource.attributes as Category, relationships, links)
            CategoryPattern::class -> CategoryPatternResourceObject(resource.id, resource.attributes as CategoryPattern, relationships, links)
            Tag::class -> TagResourceObject(resource.id, resource.attributes as Tag, relationships, links)
            TagPattern::class -> TagPatternResourceObject(resource.id, resource.attributes as TagPattern, relationships, links)
            StatementLink::class -> StatementLinkResourceObject(resource.id, resource.attributes as StatementLink, relationships, links)
            else -> throw NotImplementedError()
        }
    }

}
