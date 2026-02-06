package net.xolt.freecam.asm

import org.objectweb.asm.*

fun ClassReader.collectClassAnnotations(): List<String> {
    val result = mutableListOf<String>()
    accept(object : ClassVisitor(Opcodes.ASM9) {
        override fun visitAnnotation(desc: String, visible: Boolean) = object : AnnotationVisitor(Opcodes.ASM9) {
            init { result.add(desc) }
        }
    }, 0)
    return result
}

fun ClassReader.collectMethodAnnotations(): Map<String, List<String>> {
    val result = mutableMapOf<String, MutableList<String>>()
    accept(object : ClassVisitor(Opcodes.ASM9) {
        override fun visitMethod(access: Int, name: String, desc: String, sig: String?, ex: Array<out String>?) =
            object : MethodVisitor(Opcodes.ASM9) {
                override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
                    result.computeIfAbsent(name) { mutableListOf() }.add(desc)
                    return null
                }
            }
    }, 0)
    return result
}

fun ClassReader.collectMethodParameterAnnotations(methodName: String): Map<Int, List<String>> {
    val result = mutableMapOf<Int, List<String>>()
    accept(object : ClassVisitor(Opcodes.ASM9) {
            override fun visitMethod(
                access: Int,
                name: String,
                desc: String,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor? {
                return if (name == methodName) {
                    object : MethodVisitor(Opcodes.ASM9) {
                        override fun visitParameterAnnotation(
                            parameter: Int,
                            desc: String,
                            visible: Boolean
                        ): AnnotationVisitor? {
                            result.compute(parameter) { _, list ->
                                (list ?: emptyList()) + desc
                            }
                            return null
                        }
                    }
                } else null
            }
        }, 0)
    return result
}

fun ClassReader.collectDeclaredMethodNamesInOrder(): List<String> {
    val names = mutableListOf<String>()
    accept(object : ClassVisitor(Opcodes.ASM9) {
        override fun visitMethod(
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor? {
            names += name
            return null
        }
    }, 0)
    return names
}
