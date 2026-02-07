package net.xolt.freecam.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents how my project “publishes” or depends on a mod
 */
@Serializable
data class PublishingRelationship(
    val type: RelationshipType,
    @SerialName("cusseforge_id")
    val curseforgeId: String,
    @SerialName("modrinth_id")
    val modrinthId: String,
)

@Serializable
enum class RelationshipType {
    REQUIRED, OPTIONAL, INCLUDED, INCOMPATIBLE
}
