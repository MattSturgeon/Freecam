package net.xolt.freecam.gradle.dsl

import net.xolt.freecam.model.ModLibrary
import net.xolt.freecam.model.PublishingRelationshipSpec
import net.xolt.freecam.model.VersionConstraintSpec
import org.gradle.api.Project

sealed interface ModLibrarySpec<S : ModLibrarySpec<S>> {
    val project: Project

    /**
     * Defines a predicate indicating whether this library is available for the current project.
     * If any predicate returns `false`, then the library is not available.
     *
     * If `available == false`:
     *  - `required = false` → accessor returns `null`
     *  - `required = true`  → accessor throws [NullPointerException]
     */
    fun availableWhen(predicate: S.() -> Boolean)
}

interface BuildOnlyLibrarySpec :
    ModLibrarySpec<BuildOnlyLibrarySpec>,
    MavenCoordsAware

interface RuntimeLibrarySpec :
    ModLibrarySpec<RuntimeLibrarySpec>,
    MavenCoordsAware,
    VersionConstraintAware

interface FullLibrarySpec :
    ModLibrarySpec<FullLibrarySpec>,
    MavenCoordsAware,
    VersionConstraintAware,
    PublishingRelationshipAware

interface MavenCoordsAware {
    var group: String?
    var name: String?
    var version: String?
    var classifier: String?
    var extension: String?
}

interface VersionConstraintAware {
    val requires: VersionConstraintSpec

    /**
     * Configure a runtime version constraint for mod loaders.
     */
    fun requires(block: VersionConstraintSpec.() -> Unit) {
        requires.apply(block)
    }
}

interface PublishingRelationshipAware {
    val publishing: PublishingRelationshipSpec

    /**
     * Configures relationships for publishing platforms.
     */
    fun publishing(block: PublishingRelationshipSpec.() -> Unit) {
        publishing.apply(block)
    }
}


internal interface ModLibraryBuilder<T : ModLibrary> {
    fun build(): T?
}