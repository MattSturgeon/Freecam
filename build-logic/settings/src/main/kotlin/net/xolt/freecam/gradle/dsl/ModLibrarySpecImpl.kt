package net.xolt.freecam.gradle.dsl

import net.xolt.freecam.model.*
import org.gradle.api.Project

internal sealed class AbstractModLibrarySpecImpl<S : ModLibrarySpec<S>, T : ModLibrary> :
    ModLibraryBuilder<T>,
    ModLibrarySpec<S> {
    protected abstract val spec: S

    private val availability = mutableListOf<S.() -> Boolean>({ defaultAvailability() })

    protected open fun S.defaultAvailability(): Boolean = true

    private val available get() =
        availability.all { spec.it() }

    override fun availableWhen(predicate: S.() -> Boolean) {
        availability += predicate
    }

    abstract fun buildNonNull(): T

    override fun build(): T? =
        if (available) buildNonNull() else null
}

internal class MavenCoordsBuilder : MavenCoordsAware {
    override var group: String? = null
    override var name: String? = null
    override var version: String? = null
    override var classifier: String? = null
    override var extension: String? = null

    fun isComplete() = sequenceOf(group, name, version).all { it != null }

    internal fun build() = MavenCoords(
        group = requireNotNull(group) { "maven.group must be set" },
        name = requireNotNull(name) { "maven.name must be set" },
        version = requireNotNull(version) { "maven.version must be set" },
        classifier = classifier,
        extension = extension ?: "jar"
    )
}

internal class BuildOnlyLibrarySpecImpl(
    override val project: Project,
    private val maven: MavenCoordsBuilder = MavenCoordsBuilder(),
) : BuildOnlyLibrarySpec,
    MavenCoordsAware by maven,
    AbstractModLibrarySpecImpl<BuildOnlyLibrarySpec, BuildOnlyLibrary>() {
    override val spec get() = this

    override fun BuildOnlyLibrarySpec.defaultAvailability(): Boolean {
        return maven.isComplete()
    }

    override fun buildNonNull() = BuildOnlyLibrary(
        maven = maven.build(),
    )
}

internal class RuntimeLibrarySpecImpl(
    override val project: Project,
    private val maven: MavenCoordsBuilder = MavenCoordsBuilder(),
) : RuntimeLibrarySpec,
    MavenCoordsAware by maven,
    AbstractModLibrarySpecImpl<RuntimeLibrarySpec, RuntimeLibrary>() {
    override val spec get() = this

    override val requires = VersionConstraintSpec()

    override fun RuntimeLibrarySpec.defaultAvailability(): Boolean {
        return maven.isComplete() && sequenceOf(
            requires.maven,
            requires.semver
        ).all { it != null }
    }

    override fun buildNonNull() = RuntimeLibrary(
        maven = maven.build(),
        requires = requires.build(),
    )
}

internal class FullLibrarySpecImpl(
    override val project: Project,
    private val maven: MavenCoordsBuilder = MavenCoordsBuilder(),
) : FullLibrarySpec,
    MavenCoordsAware by maven,
    AbstractModLibrarySpecImpl<FullLibrarySpec, PublishedLibrary>() {
    override val spec get() = this

    override val requires = VersionConstraintSpec()
    override val publishing = PublishingRelationshipSpec()

    override fun FullLibrarySpec.defaultAvailability(): Boolean {
        return maven.isComplete() && sequenceOf(
            requires.maven,
            requires.semver,
            publishing.type,
            publishing.curseforgeId,
            publishing.modrinthId,
        ).all { it != null }
    }

    override fun buildNonNull() = PublishedLibrary(
        maven = maven.build(),
        requires = requires.build(),
        relationship = publishing.build(),
    )
}
