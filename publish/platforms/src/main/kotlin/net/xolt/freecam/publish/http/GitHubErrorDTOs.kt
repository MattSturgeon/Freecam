package net.xolt.freecam.publish.http

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubErrorResponse(
    val message: String,
    val errors: List<GitHubErrorDetail>? = null,
    @SerialName("documentation_url")
    val documentationUrl: String? = null,
)

@Serializable
data class GitHubErrorDetail(
    val resource: String? = null,
    val field: String? = null,
    val code: String? = null,
)

val GitHubErrorDetail.path: String
    get() = listOfNotNull(resource, field).joinToString(".")

fun GitHubErrorResponse.toException(
    status: HttpStatusCode,
    cause: Exception? = null,
) = GitHubRequestException(status, message, errors ?: emptyList(), cause)

class GitHubRequestException(
    val status: HttpStatusCode?,
    val summary: String,
    val errors: Iterable<GitHubErrorDetail>,
    cause: Exception? = null,
) : RuntimeException(
    buildString {
        append("GitHub API error: $status - $summary")
        errors.forEach { append("\n  • ${it.path}: ${it.code}") }
    },
    cause,
)