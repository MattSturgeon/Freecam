package net.xolt.freecam.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface ModLibrary {
    val maven: MavenCoords
}

@Serializable
data class BuildOnlyLibrary(
    override val maven: MavenCoords,
) : ModLibrary

@Serializable
data class RuntimeLibrary(
    override val maven: MavenCoords,
    val requires: VersionConstraint,
) : ModLibrary

@Serializable
data class PublishedLibrary(
    override val maven: MavenCoords,
    val requires: VersionConstraint,
    val relationship: PublishingRelationship,
) : ModLibrary
