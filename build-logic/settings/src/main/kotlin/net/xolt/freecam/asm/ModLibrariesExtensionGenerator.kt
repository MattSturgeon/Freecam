package net.xolt.freecam.asm

import net.xolt.freecam.gradle.AbstractModLibrariesExtension
import net.xolt.freecam.gradle.ModLibraryDefinition
import net.xolt.freecam.model.ModLibrary
import net.xolt.freecam.model.ModLibraryRegistry
import org.gradle.api.Generated
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.Type.VOID_TYPE
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.reflect.KClass

/**
 * Generates a concrete subclass of [AbstractModLibrariesExtension] using ASM.
 *
 * Each library key becomes a type-safe getter method (`getFoo()`) that delegates
 * to a [ModLibraryRegistry] that owns the corresponding [ModLibrary] instances.
 *
 * This is intended for **Gradle project DSL access**; the generated class should
 * be loaded on the buildscript classpath for Kotlin DSL compilation and instantiation.
 *
 * Future improvements:
 *  - Add Kotlin @Metadata and nullability annotations
 *  - Convert method names to proper camelCase for Kotlin property conventions
 */

private const val ABI_VERSION = 1
private const val CLASS_VERSION = Opcodes.V1_8
private const val CLASS_PACKAGE = "net.xolt.freecam.generated"
private const val CLASS_NAME = "ModLibrariesExtension"
private val SUPER_CLASS = AbstractModLibrariesExtension::class
private val LIBRARY_CLASS = ModLibrary::class
private val REGISTRY_CLASS = ModLibraryRegistry::class

private val KClass<*>.type get() = Type.getType(java)
private val KClass<*>.internalName get() = Type.getInternalName(java)
private val KClass<*>.desc get() = Type.getDescriptor(java)
private fun methodDesc(returnType: Type, vararg argumentTypes: Type) = Type.getMethodDescriptor(returnType, *argumentTypes)

@Suppress("ArrayInDataClass")
internal data class GeneratedModLibrariesAccessorClass(
    val bytes: ByteArray,
    val name: String,
    val fingerprint: String,
)

/**
 * Generates a JVM class implementing type-safe getters for each library ID.
 *
 * @param libraries set of library identifiers (e.g., `["foo", "bar-baz"]`)
 * @return raw JVM bytecode, suitable for `defineClass` or jar writing
 */
internal fun generateModLibrariesAccessorClass(libraries: List<ModLibraryDefinition<out ModLibrary>>): GeneratedModLibrariesAccessorClass {
    val abi = ModLibrariesAbiModel(libraries).apply {
        // Validate immediately before continuing
        validate()
    }

    val className = sequenceOf(
        CLASS_PACKAGE,
        abi.fingerprint.toJavaIdentifier(),
        CLASS_NAME
    ).joinToString(".")
    val internalName = className.replace('.', '/')

    val bytes = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS).apply {
        // Class header
        visit(
            CLASS_VERSION,
            Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
            internalName,
            null,
            SUPER_CLASS.internalName,
            null
        )

        // Annotate class as @Generated
        visitAnnotation(Generated::class.desc, false).visitEnd()

        // Constructor
        visitMethod(
            Opcodes.ACC_PUBLIC,
            "<init>",
            methodDesc(VOID_TYPE, REGISTRY_CLASS.type),
            null,
            null
        ).also { ctor ->
            // Name param 0 `registry` and annotate @NotNull
            ctor.visitParameter("registry", 0)
            ctor.visitParameterAnnotation(0, NotNull::class.desc, false).visitEnd()

            // Load `this` and the `registry` parameter, then invoke `super`
            ctor.visitVarInsn(Opcodes.ALOAD, 0)
            ctor.visitVarInsn(Opcodes.ALOAD, 1)
            ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, SUPER_CLASS.internalName, "<init>", methodDesc(VOID_TYPE, ModLibraryRegistry::class.type), false)
            ctor.visitInsn(Opcodes.RETURN)
            ctor.visitMaxs(2, 2)
            ctor.visitEnd()
        }

        // Generate a property getter per library
        abi.accessors.forEach { (methodName, def) ->
            visitMethod(
                Opcodes.ACC_PUBLIC,
                methodName,
                methodDesc(def.outType.type),
                null,
                null
            ).also { getter ->

                // @Nullable or @NotNull on return value
                (if (def.required) NotNull::class else Nullable::class).also { annotation ->
                    getter.visitAnnotation(annotation.desc, false).visitEnd()
                }

                // Load `this` and get its `registry` field
                getter.visitCode()
                getter.visitVarInsn(Opcodes.ALOAD, 0) // this
                getter.visitFieldInsn(Opcodes.GETFIELD, internalName, "registry", REGISTRY_CLASS.desc)
                // Load the library ID as a constant string, and invoke the registry's `get` method with it
                getter.visitLdcInsn(def.id)
                getter.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    REGISTRY_CLASS.internalName,
                    "get",
                    methodDesc(LIBRARY_CLASS.type, String::class.type),
                    true
                )

                // Unchecked cast to expected type
                getter.visitTypeInsn(Opcodes.CHECKCAST, def.outType.internalName)

                // Return the ModLibrary
                getter.visitInsn(Opcodes.ARETURN)
                getter.visitMaxs(2, 1)
                getter.visitEnd()
            }
        }

        visitEnd()
    }.toByteArray()

    return GeneratedModLibrariesAccessorClass(
        bytes = bytes,
        name = className,
        fingerprint = abi.fingerprint,
    )
}

/**
 * Internal representation of ModLibrariesExtension ABI model.
 * For use when generating accessor methods, validating and fingerprinting the class identity.
 */
internal class ModLibrariesAbiModel(
    libraries: List<ModLibraryDefinition<out ModLibrary>>,
    val abiVersion: Int = ABI_VERSION,
    val jvmVersion: Int = CLASS_VERSION,
) {

    private fun String.methodName() =
        ("get" + toPascalCase()).toJavaIdentifier()

    /**
     * Internal store representing methodName → definition.
     * Keys with multiple entries are conflicts that will cause [validate] to throw.
     */
    private val byGetter by lazy {
        libraries.groupBy { it.id.methodName() }
    }

    private val conflicts get() = byGetter
        .filterValues { it.size > 1 }
        .map { (getter, defs) ->
            val ids = defs.joinToString(", ") {
                "'${it.id}' (${it.outType.java.simpleName})"
            }
            "  - accessor $getter ← $ids"
        }

    val accessors get() = byGetter.mapValues { it.value.single() }.toSortedMap()

    val fingerprint: String get() {
        val canonical = buildString {
            append("abi=").append(abiVersion).appendLine()
            append("jvm=").append(jvmVersion).appendLine()
            accessors.forEach { (methodName, def) ->
                append(methodName)
                append(' ')
                append(def.id)
                append(' ')
                append(def.outType.java.name)
                append(' ')
                append(if (def.required) "NotNull" else "Nullable")
                appendLine()
            }
        }

        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))

        return Base64.UrlSafe
            .withPadding(Base64.PaddingOption.ABSENT)
            .encode(digest, 0, 16)
    }

    fun validate() {
        val conflicts = conflicts
        if (conflicts.isEmpty()) return
        throw IllegalArgumentException(
            "Multiple library definitions resolve to the same accessor name.\n"
            + conflicts.joinToString("\n\n")
        )
    }
}
