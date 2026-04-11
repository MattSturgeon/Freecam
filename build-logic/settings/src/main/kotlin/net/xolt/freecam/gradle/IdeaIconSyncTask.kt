package net.xolt.freecam.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class IdeaIconSyncTask : DefaultTask() {

    @get:InputFile
    abstract val sourceFile: RegularFileProperty

    @get:OutputFile
    abstract val targetFile: RegularFileProperty

    init {
        sourceFile.finalizeValueOnRead()
        targetFile.finalizeValueOnRead()
        targetFile.convention(project.layout.projectDirectory.file(".idea/icon.png"))
    }

    @TaskAction
    fun sync() {
        val src = sourceFile.asFile.get()
        val target = targetFile.asFile.get()

        if (!src.exists()) {
            logger.error("Idea icon source does not exist: $src")
            return
        }

        if (target.exists() && src contentEquals target) {
            logger.debug("Idea icon is up to date")
            return
        }

        target.parentFile.mkdirs()
        src.copyTo(target, overwrite = true)

        logger.info("Synced IDEA icon: $src -> $target")
    }

    private infix fun File.contentEquals(otherFile: File): Boolean {
        val bufferSize = 1024 * 1024
        val aBytes = ByteArray(bufferSize)
        val bBytes = ByteArray(bufferSize)
        inputStream().use { a ->
            otherFile.inputStream().use { b ->
                while (true) {
                    val aCount = a.read(aBytes)
                    val bCount = b.read(bBytes)
                    when {
                        aBytes contentEquals bBytes -> {
                            // Check for end of file
                            val aEOF = aCount < bufferSize
                            val bEOF = bCount < bufferSize
                            if (aEOF || bEOF) return aEOF == bEOF
                        }
                        else -> return false
                    }
                }
            }
        }
    }
}
