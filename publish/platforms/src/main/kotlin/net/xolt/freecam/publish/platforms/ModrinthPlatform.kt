package net.xolt.freecam.publish.platforms

import net.xolt.freecam.model.ReleaseMetadata
import net.xolt.freecam.publish.model.ReleaseArtifact

interface ModrinthPlatform : Platform { companion object }

fun ModrinthPlatform.Companion.create(dryRun: Boolean = false) =
    if (dryRun) DryRunModrinthPlatform()
    else DefaultModrinthPlatform()

internal class DryRunModrinthPlatform : ModrinthPlatform {
    override suspend fun publishRelease(metadata: ReleaseMetadata, artifacts: List<ReleaseArtifact>) {
        artifacts.forEach {
            println("[dry-run][modrinth] ${metadata.modVersion} upload ${it.artifact}")
        }
    }
}

internal class DefaultModrinthPlatform : ModrinthPlatform {
    override suspend fun publishRelease(metadata: ReleaseMetadata, artifacts: List<ReleaseArtifact>) {
        TODO("Not yet implemented")
    }
}
