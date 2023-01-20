package dev.benedikt.plutos.api.structure

import dev.benedikt.plutos.models.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Resource

@Serializable
sealed class ResourceObject {
    var id: Int?
    val relationships: Map<String, RelationshipObject>
    val links: Links?

    constructor(id: Int?, relationships: Map<String, RelationshipObject>, links: Links?) {
        this.id = id
        this.relationships = relationships
        this.links = links
    }
}

@SerialName(Account.type)
@Serializable
class AccountResourceObject : ResourceObject {
    val attributes: Account
    constructor(id: Int?, attributes: Account, relationships: Map<String, RelationshipObject>, links: Links?) : super(id, relationships, links) {
        this.attributes = attributes
    }
}

@SerialName(Statement.type)
@Serializable
class StatementResourceObject : ResourceObject {
    var attributes: Statement
    constructor(id: Int?, attributes: Statement, relationships: Map<String, RelationshipObject>, links: Links?) : super(id, relationships, links) {
        this.attributes = attributes
    }
}

@SerialName(Category.type)
@Serializable
class CategoryResourceObject : ResourceObject {
    val attributes: Category
    constructor(id: Int?, attributes: Category, relationships: Map<String, RelationshipObject>, links: Links?) : super(id, relationships, links) {
        this.attributes = attributes
    }
}

@SerialName(CategoryPattern.type)
@Serializable
class CategoryPatternResourceObject : ResourceObject {
    val attributes: CategoryPattern
    constructor(id: Int?, attributes: CategoryPattern, relationships: Map<String, RelationshipObject>, links: Links?) : super(id, relationships, links) {
        this.attributes = attributes
    }
}

@SerialName(Tag.type)
@Serializable
class TagResourceObject : ResourceObject {
    val attributes: Tag
    constructor(id: Int?, attributes: Tag, relationships: Map<String, RelationshipObject>, links: Links?) : super(id, relationships, links) {
        this.attributes = attributes
    }
}

@SerialName(TagPattern.type)
@Serializable
class TagPatternResourceObject : ResourceObject {
    val attributes: TagPattern
    constructor(id: Int?, attributes: TagPattern, relationships: Map<String, RelationshipObject>, links: Links?) : super(id, relationships, links) {
        this.attributes = attributes
    }
}

@SerialName(StatementLink.type)
@Serializable
class StatementLinkResourceObject : ResourceObject {
    val attributes: StatementLink
    constructor(id: Int?, attributes: StatementLink, relationships: Map<String, RelationshipObject>, links: Links?) : super(id, relationships, links) {
        this.attributes = attributes
    }
}

@Serializable
data class ResourceWrapper<T : ResourceObject>(val data: T)
