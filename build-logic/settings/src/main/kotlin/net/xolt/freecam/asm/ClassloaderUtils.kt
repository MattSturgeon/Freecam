package net.xolt.freecam.asm

import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

/**
 * Define a class in this ClassLoader using raw JVM bytecode.
 *
 * WARNING: This uses reflection to invoke the protected `defineClass` method.
 * Use carefully to avoid double-loading classes or breaking Gradle's classloader hierarchy.
 *
 * @param className fully qualified JVM name (e.g., `"net.xolt.freecam.generated.ModLibraryExtension"`)
 * @param classBytes raw JVM bytecode for the class
 * @return the loaded [Class] object
 */
internal fun ClassLoader.loadClassFromBytes(className: String, classBytes: ByteArray): Class<*> =
    ClassLoader::class.java
        .getDeclaredMethod(
            "defineClass",
            String::class.java,
            ByteArray::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )
        .apply { isAccessible = true }
        .invoke(this, className, classBytes, 0, classBytes.size) as Class<*>

/**
 * Gets the specified class from this ClassLoader if it is loaded.
 * If it is *not* loaded, [classBytesSupplier] is used to load raw JVM bytecode.
 *
 * WARNING: This uses reflection to invoke the protected `defineClass` method.
 * Use carefully to avoid double-loading classes or breaking Gradle's classloader hierarchy.
 *
 * @param className fully qualified JVM name (e.g., `"net.xolt.freecam.generated.ModLibraryExtension"`)
 * @param classBytesSupplier provides raw JVM bytecode for the class
 * @return the loaded [Class] object
 */
internal fun ClassLoader.findOrLoadClass(className: String, classBytesSupplier: () -> ByteArray): Class<*> =
    try {
        loadClass(className)
    } catch (_: ClassNotFoundException) {
        loadClassFromBytes(className, classBytesSupplier())
    }

/**
 * Writes a class provided as JVM bytecode to this file, formatting as a Jar archive.
 *
 * @param className fully qualified JVM name (e.g., `"net.xolt.freecam.generated.ModLibraryExtension"`)
 * @param classBytes raw JVM bytecode to write into the Jar
 */
internal fun File.writeClassAsJar(className: String, classBytes: ByteArray) {
    JarOutputStream(outputStream()).use { jar ->
        jar.putNextEntry(JarEntry("${className.replace('.', '/')}.class"))
        jar.write(classBytes)
        jar.closeEntry()
    }
}
