package net.xolt.freecam.model

import net.xolt.freecam.gradle.ModLibraryDefinition
import org.gradle.api.Project

/**
 * Registry for looking up configured [ModLibrary] instances by ID.
 *
 * Implementations must ensure that returned instances:
 *  - are stable per project
 *  - match the ABI-declared type
 *  - respect required/optional semantics
 */
interface ModLibraryRegistry {
    /**
     * Returns the library instance for the given ID.
     *
     * @throws NullPointerException
     *   if the library is required by the ABI but not available for this project.
     *
     * @throws ClassCastException
     *   if the resolved library does not match the ABI-declared type.
     */
    operator fun get(id: String): ModLibrary?
}

internal class DefaultModLibraryRegistry(
    private val project: Project,
    private val definitions: Map<String, ModLibraryDefinition<out ModLibrary>>,
) : ModLibraryRegistry {
    private val instances = mutableMapOf<String, ModLibrary?>()

    // TODO: should we do the null/type checks on every access, or push that into the `getOrPut` lambda?
    override fun get(id: String): ModLibrary? {
        val def = requireNotNull(definitions[id]) {
            "No library registered with ID '$id'"
        }

        val lib = instances.getOrPut(id) { def.factory(project) }

        if (lib == null) {
            if (def.required) {
                throw NullPointerException(
                    "Required mod library '$id' (${def.outType.simpleName}) is not available for this project"
                )
            }
            return null
        }

        if (!def.outType.isInstance(lib)) {
            throw ClassCastException(
                "Mod library '$id' resolved to ${lib::class.java.name}, "
                        + "but ABI expects ${def.outType.java.name}"
            )
        }

        return lib
    }
}
