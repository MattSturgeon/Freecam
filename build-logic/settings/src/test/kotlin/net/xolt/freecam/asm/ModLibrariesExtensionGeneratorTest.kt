@file:Suppress("UNCHECKED_CAST")

package net.xolt.freecam.asm

import groovyjarjarasm.asm.Type
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldContainOnlyOnce
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import net.xolt.freecam.gradle.AbstractModLibrariesExtension
import net.xolt.freecam.gradle.ModLibraryDefinition
import net.xolt.freecam.model.*
import org.gradle.api.Generated
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.objectweb.asm.ClassReader
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.test.Test

/*
 * These tests intentionally focus on:
 *  - JVM ABI stability
 *  - Bytecode correctness at the public surface
 *  - Functional integration via the injected registry
 */

private val GRADLE_GENERATED_DESC = Type.getDescriptor(Generated::class.java)
private val NOT_NULL_DESC = Type.getDescriptor(NotNull::class.java)
private val NULLABLE_DESC = Type.getDescriptor(Nullable::class.java)

private fun def(
    id: String,
    required: Boolean = true,
    outType: KClass<out ModLibrary> = BuildOnlyLibrary::class,
): ModLibraryDefinition<out ModLibrary> =
    ModLibraryDefinition(
        id = id,
        required = required,
        outType = outType,
        factory = { error("factory must not be invoked during ASM tests") }
    )


class ModLibrariesExtensionGeneratorTest {

    /**
     * Define the generated class in an isolated child ClassLoader so:
     *  - we don't pollute the test JVM
     *  - we exercise real JVM class loading
     */
    private fun loadGeneratedClass(result: GeneratedModLibrariesAccessorClass): Class<*> {
        val loader = object : ClassLoader(this::class.java.classLoader) {
            fun define(name: String, bytes: ByteArray): Class<*> =
                defineClass(name, bytes, 0, bytes.size)
        }
        return loader.define(result.name, result.bytes)
    }

    @Test
    fun `generated class has correct name and superclass`() {
        val result = generateModLibrariesAccessorClass(listOf(def("foo")))
        val clazz = loadGeneratedClass(result)

        clazz.name shouldBe result.name
        clazz.name shouldEndWith ".ModLibrariesExtension"
        clazz.superclass shouldBe AbstractModLibrariesExtension::class.java
    }

    @Test
    fun `generated class is public and final`() {
        val clazz = loadGeneratedClass(
            generateModLibrariesAccessorClass(listOf(def("foo")))
        )

        Modifier.isPublic(clazz.modifiers) shouldBe true
        Modifier.isFinal(clazz.modifiers) shouldBe true
    }

    @Test
    fun `generated constructor accepts ModLibraryRegistry`() {
        val clazz = loadGeneratedClass(
            generateModLibrariesAccessorClass(listOf(def("foo")))
        )

        shouldNotThrowAny {
            clazz.getConstructor(ModLibraryRegistry::class.java)
        }
    }

    @Test
    fun `constructor parameter name is emitted for debugging`() {
        val clazz = loadGeneratedClass(
            generateModLibrariesAccessorClass(listOf(def("foo")))
        )

        val ctor = clazz.getConstructor(ModLibraryRegistry::class.java)
        ctor.parameters.single().name shouldBe "registry"
    }

    @Test
    fun `getter names are formatted then sanitised`() {
        val defs = listOf(
            def("foo"),
            def("bar-baz"),
            def("hello_world!"),
            def("weird.id!")
        )

        val clazz = loadGeneratedClass(
            generateModLibrariesAccessorClass(defs)
        )

        val getters = clazz.declaredMethods
            .filter { it.parameterCount == 0 }
            .map { it.name }

        getters shouldContainExactlyInAnyOrder listOf(
            "getBarBaz",
            "getFoo",
            "getHelloWorld_",
            "getWeird_id_"
        )
    }

    @Test
    fun `getters are declared in order`() {
        val defs = listOf(
            def("zoo"),
            def("bar-baz"),
            def("hello_world!"),
            def("foo_bar"),
            def("bar"),
            def("foo"),
        )

        val generated = generateModLibrariesAccessorClass(defs)
        val getters = ClassReader(generated.bytes)
            .collectDeclaredMethodNamesInOrder()
            .filter { it.startsWith("get") }

        getters shouldBe listOf(
            "getBar",
            "getBarBaz",
            "getFoo",
            "getFooBar",
            "getHelloWorld_",
            "getZoo",
        )
    }

    @Test
    fun `getter methods delegate to registry with correct library ID`() {
        val foo = BuildOnlyLibrary(MavenCoords("g", "foo", "1"))
        val bar = BuildOnlyLibrary(MavenCoords("g", "bar", "2"))

        val defs = listOf(def("foo"), def("bar"))
        val clazz = loadGeneratedClass(generateModLibrariesAccessorClass(defs))

        val registry = mockk<ModLibraryRegistry>()
        every { registry["foo"] } returns foo
        every { registry["bar"] } returns bar

        val instance = clazz
            .getConstructor(ModLibraryRegistry::class.java)
            .newInstance(registry)

        // Verify returned values match what the registry provided
        clazz.getMethod("getFoo").invoke(instance) shouldBeSameInstanceAs foo
        clazz.getMethod("getBar").invoke(instance) shouldBeSameInstanceAs bar

        // Verify registry was used correctly
        verifySequence {
            registry["foo"]
            registry["bar"]
        }
    }

    @Test
    fun `only declared library getters are generated`() {
        val result = generateModLibrariesAccessorClass(listOf(
            def("foo"),
        ))
        val clazz = loadGeneratedClass(result)

        // NOTE: We intentionally ignore inherited methods not owned by codegen.
        val getters = clazz.declaredMethods
            .asSequence()
            .filter { it.name.startsWith("get") }
            .filter { it.parameterCount == 0 }
            .map { it.name }
            .toList()

        getters shouldContainExactly listOf("getFoo")
    }

    @Test
    fun `throws when two libraries map to the same getter name`() {
        val defs = listOf(
            def("foo-bar"),
            def("foo_bar"),
        )

        val ex = shouldThrow<IllegalArgumentException> {
            generateModLibrariesAccessorClass(defs)
        }

        ex.message shouldContain "getFooBar"
        ex.message shouldContain "'foo-bar' (BuildOnlyLibrary)"
        ex.message shouldContain "'foo_bar' (BuildOnlyLibrary)"
    }

    // Logical == set equality, not iteration order
    @Test
    fun `generation is deterministic for same logical input`() {
        val a = listOf(def("foo"), def("bar"), def("baz"))
        val b = listOf(def("baz"), def("foo"), def("bar"))

        val first = generateModLibrariesAccessorClass(a)
        val second = generateModLibrariesAccessorClass(b)

        // NOTE: This asserts *byte-level* reproducibility.
        first.bytes.contentEquals(second.bytes) shouldBe true
        first.fingerprint shouldBe second.fingerprint
    }

    @Test
    fun `fingerprint changes when input changes`() {
        val first = generateModLibrariesAccessorClass(listOf(def("a"), def("b")))
        val second = generateModLibrariesAccessorClass(listOf(def("x"), def("y")))

        first.fingerprint shouldNotBe second.fingerprint
    }

    @Test
    fun `class name contains fingerprint`() {
        val result = generateModLibrariesAccessorClass(
            listOf(def("foo"), def("bar"))
        )

        result.name shouldContainOnlyOnce result.fingerprint.toJavaIdentifier()
    }

    @Test
    fun `generated class is annotated @Generated`() {
        val generated = generateModLibrariesAccessorClass(listOf(def("foo")))
        val annotations = ClassReader(generated.bytes).collectClassAnnotations()

        annotations shouldContainExactlyInAnyOrder listOf(GRADLE_GENERATED_DESC)
    }

    @Test
    fun `getter nullability matches required flag`() {
        val defs = listOf(
            def("required", required = true),
            def("optional", required = false),
        )

        val annotations = ClassReader(
            generateModLibrariesAccessorClass(defs).bytes
        ).collectMethodAnnotations()

        annotations["getRequired"] shouldBe listOf(NOT_NULL_DESC)
        annotations["getOptional"] shouldBe listOf(NULLABLE_DESC)
    }

    @Test
    fun `generated class can be instantiated with fake registry`() {
        val clazz = loadGeneratedClass(
            generateModLibrariesAccessorClass(listOf(def("foo")))
        )

        val registry = object : ModLibraryRegistry {
            override fun get(id: String): ModLibrary =
                BuildOnlyLibrary(MavenCoords("g", id, "1"))
        }

        val instance = clazz
            .getConstructor(ModLibraryRegistry::class.java)
            .newInstance(registry)

        val lib = clazz.getMethod("getFoo").invoke(instance) as BuildOnlyLibrary
        lib.maven.name shouldBe "foo"
    }

    @Test
    fun `getter return type matches library outType`() {
        val defs = listOf(
            def("runtime", outType = RuntimeLibrary::class),
            def("published", outType = PublishedLibrary::class),
        )

        val clazz = loadGeneratedClass(generateModLibrariesAccessorClass(defs))

        clazz.getMethod("getRuntime").returnType shouldBe RuntimeLibrary::class.java
        clazz.getMethod("getPublished").returnType shouldBe PublishedLibrary::class.java
    }
}
