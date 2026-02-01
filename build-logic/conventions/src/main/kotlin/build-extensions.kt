import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import dev.kikugie.stonecutter.data.tree.ProjectNode
import net.xolt.freecam.gradle.FreecamModExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.maven

fun Project.prop(key: String): String? = findProperty(key)?.toString()

fun RepositoryHandler.strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
    forRepository { maven(url) { name = alias } }
    filter { groups.forEach(::includeGroup) }
}

/**
 * Alias for [`mod`][FreecamModExtension], as seen outside build-logic.
 */
internal val Project.mod: FreecamModExtension get() = extensions.getByType<FreecamModExtension>()

/**
 * The `stonecutter` extension on :loader:version projects is a [StonecutterBuildExtension].
 * This binding provides access within build-logic.
 */
internal val Project.stonecutter get() = extensions.getByType<StonecutterBuildExtension>()

/**
 * The `stonecutter` extension in `stonecutter.gradle.kts` is a [StonecutterControllerExtension].
 * This binding provides access within build-logic.
 */
internal val Project.stonecutterController get() = extensions.getByType<StonecutterControllerExtension>()

/**
 * The stonecutter [ProjectNode] for the current version's `:common` project, e.g. `:common:1.12.11`.
 */
val Project.commonNode: ProjectNode get() = requireNotNull(stonecutter.node.sibling("common")) {
    "No common project for $project"
}

/**
 * The current version's `rootProject`, e.g. `project(":1.21.11")`.
 */
val Project.currentRootProject get() = rootProject.project(stonecutter.current.project)!!

/**
 * [Project.mod] for the [current version's root project][Project.currentRootProject].
 */
val Project.currentMod get() = currentRootProject.mod

val Project.requiredJava get() = when {
    stonecutter.current.parsed >= "1.20.6" -> JavaVersion.VERSION_21
    stonecutter.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    stonecutter.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}
val Project.javaVersion get() = requiredJava.majorVersion.toInt()
val Project.javaLanguageVersion get() = JavaLanguageVersion.of(javaVersion)

val Project.commonExpansions: Map<String, String>
    get() {
        return mapOf(
            "mixinCompatLevel" to "JAVA_$javaVersion",
            "modId" to mod.meta.id,
            "modName" to mod.meta.name,
            "modVersion" to mod.meta.version,
            "modGroup" to mod.meta.group,
            "modAuthors" to mod.meta.authors.joinToString(", "),
            "modDescription" to mod.meta.description,
            "modLicense" to mod.meta.license,
            "modHomepage" to mod.meta.homepageUrl.toString(),
            "modSource" to mod.meta.sourceUrl.toString(),
            "modIssues" to mod.meta.issuesUrl.toString(),
            "modGhReleases" to mod.meta.githubReleasesUrl.toString(),
            "modCurseforge" to mod.meta.curseforgeUrl.toString(),
            "modModrinth" to mod.meta.modrinthUrl.toString(),
            "modCrowdin" to mod.meta.crowdinUrl.toString(),
            "minecraftVersion" to currentMod.propOrNull("minecraft_version"),
            "fabricLoaderVersion" to currentMod.depOrNull("fabric_loader"),
            "fabricLoaderReq" to currentMod.propOrNull("fabric_loader_req"),
            "fabricMcReq" to currentMod.propOrNull("fabric_mc_req"),
            "fabricApiVersion" to currentMod.depOrNull("fabric_api"),
            "neoForgeVersion" to currentMod.depOrNull("neoforge"),
            "neoforgeLoaderReq" to currentMod.propOrNull("neoforge_loader_req"),
            "neoforgeReq" to currentMod.propOrNull("neoforge_req"),
            "neoforgeMcReq" to currentMod.propOrNull("neoforge_mc_req"),
            "forgeVersion" to currentMod.depOrNull("forge"),
            "forgeLoaderReq" to currentMod.propOrNull("forge_loader_req"),
            "forgeReq" to currentMod.propOrNull("forge_req"),
            "forgeMcReq" to currentMod.propOrNull("forge_mc_req"),
        ).filterValues { it?.isNotEmpty() == true }.mapValues { it.value!! }
    }

// TODO: handle JSON as structured data, to avoid string injection hacks
val Project.commonJsonExpansions get() = buildMap {
    putAll(project.commonExpansions)
    mapValues { (_, v) -> v.replace("\n", "\\\\n") }
    put("modAuthorsJson", mod.meta.authors.joinToString("\", \""))
}