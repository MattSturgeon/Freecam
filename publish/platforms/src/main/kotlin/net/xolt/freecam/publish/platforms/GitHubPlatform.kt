package net.xolt.freecam.publish.platforms

import io.ktor.http.*
import net.xolt.freecam.model.ReleaseMetadata
import net.xolt.freecam.model.ReleaseType
import net.xolt.freecam.publish.http.GitHubRelease
import net.xolt.freecam.publish.http.GitHubReleaseAsset
import net.xolt.freecam.publish.http.GitHubReleasesClient
import net.xolt.freecam.publish.http.JavaArchive
import net.xolt.freecam.publish.model.GitHubConfig
import net.xolt.freecam.publish.model.ReleaseArtifact
import kotlin.io.path.name
import kotlin.io.path.readBytes

interface GitHubPlatform : Platform { companion object }

fun GitHubPlatform.Companion.create(dryRun: Boolean = false, config: GitHubConfig) =
    if (dryRun) DryRunGitHubPlatform(config)
    else DefaultGitHubPlatform(config)

internal class DryRunGitHubPlatform(
    private val config: GitHubConfig,
) : GitHubPlatform {
    override suspend fun publishRelease(metadata: ReleaseMetadata, artifacts: List<ReleaseArtifact>) {
        println("[dry-run][github] using config $config")
        println("[dry-run][github] creating release ${metadata.platforms.github.tag}")

        artifacts.forEach {
            println("[dry-run][github] uploading ${it.artifact} (${it.size} bytes) with SHA-256: ${it.sha256.toHexString()}")
        }
    }
}

internal class DefaultGitHubPlatform(
    private val config: GitHubConfig,
    private val client: GitHubReleasesClient = GitHubReleasesClient(config),
) : GitHubPlatform, AutoCloseable {

    override suspend fun publishRelease(
        metadata: ReleaseMetadata,
        artifacts: List<ReleaseArtifact>,
    ) {
        // Get the release
        val release = reconcileRelease(metadata)

        // Upload assets
        reconcileAssets(release, artifacts).forEach { artifact ->
            val result = client.uploadAssetToRelease(
                release = release,
                name = artifact.artifact.name,
                contentType = ContentType.Application.JavaArchive,
                content = artifact.artifact.readBytes(),
            )

            require(artifact.contentMatches(result)) {
                val diff = result.sha256
                    ?.let { "artifact: ${artifact.sha256.toHexString()}, uploaded: ${it.toHexString()}" }
                    ?: "artifact size: ${artifact.size}, uploaded size: ${result.size}"
                "Uploaded asset mismatch for ${artifact.artifact.name} ($diff)"
            }
        }

        // Finally, undraft
        if (release.draft) client.updateDraftState(release, false)
    }

    internal suspend fun reconcileRelease(
        metadata: ReleaseMetadata,
        draft: Boolean = true,
    ): GitHubRelease {
        val tag = metadata.platforms.github.tag
        val prerelease = ReleaseType.valueOf(metadata.releaseType.uppercase()) != ReleaseType.RELEASE

        // Try to fetch existing release by tag
        val existingId = client.getReleaseIDByTag(tag)

        // Patch existing release metadata
        val patched = existingId?.let {
            client.updateRelease(
                releaseId = existingId,
                tagName = tag,
                targetCommitish = config.headSha,
                name = metadata.displayName,
                body = metadata.changelog,
                prerelease = prerelease,
            )
        }

        // Create if not exists
        return patched ?: client.createRelease(
            tagName = tag,
            targetCommitish = config.headSha,
            name = metadata.displayName,
            body = metadata.changelog,
            prerelease = prerelease,
            draft = draft,
        )
    }

    internal suspend fun reconcileAssets(
        release: GitHubRelease,
        artifacts: List<ReleaseArtifact>,
    ): List<ReleaseArtifact> = buildList {
        val existingAssets = release.assets.associateBy { it.name }
        for (artifact in artifacts) {
            val existing = existingAssets.getOrElse(artifact.artifact.name) {
                // Not uploaded yet
                add(artifact)
                continue
            }
            // Already uploaded, replace if conflicting
            if (artifact conflictsWith existing) {
                client.deleteAsset(existing)
                add(artifact)
            }
        }
    }

    override fun close() = client.close()
}

private infix fun ReleaseArtifact.conflictsWith(asset: GitHubReleaseAsset) =
    !contentMatches(asset)

private fun ReleaseArtifact.contentMatches(asset: GitHubReleaseAsset): Boolean =
    sequenceOf(
        asset.size == size,
        asset.sha256?.let(sha256::contentEquals),
    ).filterNotNull().all { it }
