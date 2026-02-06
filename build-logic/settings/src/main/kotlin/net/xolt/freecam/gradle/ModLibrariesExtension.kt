package net.xolt.freecam.gradle

import net.xolt.freecam.model.DefaultModLibraryRegistry
import net.xolt.freecam.model.ModLibrary
import net.xolt.freecam.model.ModLibraryRegistry
import org.gradle.api.Project

/**
 * Base class for the generated mod libraries project extension.
 *
 * Concrete subclasses are generated at runtime to provide type-safe accessors
 * for each library defined in settings.
 *
 * @property registry Registry for looking up [ModLibrary] instances by ID.
 *                    Typically, this is a [Project]-specific instance of [DefaultModLibraryRegistry].
 */
abstract class AbstractModLibrariesExtension(
    @JvmField protected val registry: ModLibraryRegistry
)