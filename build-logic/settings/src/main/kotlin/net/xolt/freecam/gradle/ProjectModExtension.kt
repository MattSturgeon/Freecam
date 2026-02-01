package net.xolt.freecam.gradle

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import net.xolt.freecam.model.ModMetadata
import net.xolt.freecam.model.ParchmentVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

data class SettingsModExtension(
    override val meta: ModMetadata,
) : BaseFreecamModExtension

class ProjectModExtension(
    private val project: Project,
    override val meta: ModMetadata,
) : FreecamModExtension {
    private val stonecutter by lazy {
        project.extensions.findByType<StonecutterBuildExtension>()
    }

    override val mc by lazy {
        depOrNull("minecraft")
            ?: requireNotNull(stonecutter) {
                "Cannot access `mc` in ${project.path} - no stonecutter extension"
            }.current.version
    }

    override val loader by lazy {
        propOrNull("loader")
            ?: requireNotNull(stonecutter) {
                "Cannot access `loader` in ${project.path} - no stonecutter extension"
            }.branch.id
    }

    override val parchment by lazy {
        depOrNull("parchment")?.let(ParchmentVersion::parse)
    }

    override fun parchment(block: (mappings: String, minecraft: String) -> Unit) {
        parchment?.let { block(it.mappings, it.minecraft ?: mc) }
    }

    override fun propOrNull(key: String) = project.findProperty(key) as String?
    override fun prop(key: String) = requireNotNull(propOrNull(key)) { "Missing '$key'" }
    override fun modPropOrNull(key: String) = propOrNull("mod.$key")
    override fun modProp(key: String) = requireNotNull(modPropOrNull(key)) { "Missing 'mod.$key'" }
    override fun depOrNull(key: String): String? = propOrNull("deps.$key")?.takeUnless { it.isEmpty() }
    override fun dep(key: String) = requireNotNull(depOrNull(key)) { "Missing 'deps.$key'" }
}
