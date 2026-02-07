package net.xolt.freecam.model

import kotlinx.serialization.Serializable

/**
 * Represents the artifact coordinates of a dependency
 */
@Serializable
data class MavenCoords(
    val group: String,
    val name: String,
    val version: String,
    val classifier: String? = null,
    val extension: String = "jar"
) {
    /**
     * Returns standard Maven coordinate string.
     */
    val coordinate get() = buildString {
        sequenceOf(group, name, version, classifier)
            .filterNotNull()
            .joinToString(":")
            .let(::append)
        if (extension != "jar") {
            append("@")
            append(extension)
        }
    }
}
