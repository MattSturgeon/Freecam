package net.xolt.freecam.model

import kotlinx.serialization.Serializable

// TODO: represent semantically (AtLeast, Approx, Exact)
// For now we encode the raw semver/maven formats

/**
 * Represents the runtime version requirement my mod has on this dependency
 */
@Serializable
data class VersionConstraint(
    val semver: String,
    val maven: String,
)
