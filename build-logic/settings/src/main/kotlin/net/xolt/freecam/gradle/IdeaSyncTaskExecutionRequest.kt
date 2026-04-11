package net.xolt.freecam.gradle

import org.gradle.TaskExecutionRequest
import org.gradle.api.initialization.ProjectDescriptor
import java.io.File
import java.io.Serializable

data class IdeaSyncTaskExecutionRequest(
    private val args: List<String>,
    private val projectPath: String? = null,
    private val rootProjectPath: File? = null,
) : TaskExecutionRequest, Serializable {

    companion object {
        fun of(project: ProjectDescriptor, vararg task: String) = IdeaSyncTaskExecutionRequest(
            task.toList(),
            project.path,
            project.projectDir,
        )
    }

    override fun getArgs(): List<String> {
        return args.toList()
    }

    override fun getProjectPath(): String? {
        return projectPath
    }

    override fun getRootDir(): File? {
        return rootProjectPath
    }
}