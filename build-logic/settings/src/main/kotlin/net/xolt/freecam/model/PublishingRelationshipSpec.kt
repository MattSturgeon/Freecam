package net.xolt.freecam.model

/**
 * Configuration for a [PublishingRelationship].
 */
class PublishingRelationshipSpec {

    var type: RelationshipType? = null
    var curseforgeId: String? = null
    var modrinthId: String? = null

    internal fun build() = PublishingRelationship(
        type = requireNotNull(type) { "Publishing relationship type must be set" },
        curseforgeId = requireNotNull(curseforgeId) { "Curseforge ID must be set" },
        modrinthId = requireNotNull(modrinthId) { "Modrinth ID must be set" },
    )
}