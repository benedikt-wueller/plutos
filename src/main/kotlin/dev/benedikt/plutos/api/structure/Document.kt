package dev.benedikt.plutos.api.structure

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

interface Document

@Serializable
data class SuccessSingleDocument(val links: Links?, val data: ResourceObject?) : Document

@Serializable
data class SuccessMultiDocument(val links: Links?, val data: List<ResourceObject>) : Document

@Serializable
data class ErrorDocument(val links: Links?, val errors: List<ErrorObject>) : Document

@Serializable
data class Links(val self: String? = null, val related: String? = null)

private fun ApplicationCall.createLinks(includeLinks: Boolean): Links? {
    if (!includeLinks) return null
    val origin = this.request.origin
    return Links(self = "${origin.scheme}://${origin.serverHost}:${origin.serverPort}${origin.uri}")
}

suspend fun ApplicationCall.respondDocument(statusCode: HttpStatusCode, data: ResourceObject? = null, includeLinks: Boolean = true) {
    val response = SuccessSingleDocument(createLinks(includeLinks), data)
    this.respond(statusCode, response)
}

suspend fun ApplicationCall.respondDocument(statusCode: HttpStatusCode, data: List<ResourceObject>, includeLinks: Boolean = true) {
    val response = SuccessMultiDocument(createLinks(includeLinks), data)
    this.respond(statusCode, response)
}

suspend fun ApplicationCall.respondError(statusCode: HttpStatusCode, vararg errors: ErrorObject, includeLinks: Boolean = true) {
    val response = ErrorDocument(createLinks(includeLinks), errors.toList())
    this.respond(statusCode, response)
}
