package net.xolt.freecam.model

/**
 * Configuration for [VersionConstraint].
 */
class VersionConstraintSpec {

    var semver: String? = null
    var maven: String? = null

    internal fun build() = VersionConstraint(
        semver = requireNotNull(semver) { "Version semver must be set" },
        maven = requireNotNull(maven) { "Version maven must be set" },
    )
}