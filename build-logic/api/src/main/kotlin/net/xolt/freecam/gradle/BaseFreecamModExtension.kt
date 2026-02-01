package net.xolt.freecam.gradle

import net.xolt.freecam.model.ModMetadata
import net.xolt.freecam.model.ParchmentVersion

interface BaseFreecamModExtension {
    val meta: ModMetadata
}

interface FreecamModExtension : BaseFreecamModExtension {
    val mc: String
    val loader: String

    val parchment: ParchmentVersion?

    fun parchment(block: (mappings: String, minecraft: String) -> Unit)

    fun propOrNull(key: String): String?
    fun prop(key: String): String
    fun modPropOrNull(key: String): String?
    fun modProp(key: String): String
    fun depOrNull(key: String): String?
    fun dep(key: String): String
}
