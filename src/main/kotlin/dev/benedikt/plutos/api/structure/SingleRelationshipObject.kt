package dev.benedikt.plutos.api.structure

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

object RelationshipSerializer : JsonContentPolymorphicSerializer<RelationshipObject>(RelationshipObject::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out RelationshipObject> {
        val json = element.jsonObject
        return when {
            json.containsKey("data") && json["data"] is JsonArray -> MultiRelationshipObject.serializer()
            else -> SingleRelationshipObject.serializer()
        }
    }
}

@Serializable(RelationshipSerializer::class)
sealed class RelationshipObject {
    abstract val links: Links?
}

@Serializable
class SingleRelationshipObject(override val links: Links?, val data: ResourceIdentifierObject?) : RelationshipObject()

@Serializable
class MultiRelationshipObject(override val links: Links?, val data: List<ResourceIdentifierObject>) : RelationshipObject()
