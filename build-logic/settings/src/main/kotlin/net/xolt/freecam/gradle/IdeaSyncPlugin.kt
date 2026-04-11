package net.xolt.freecam.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.register

abstract class IdeaSyncPlugin : Plugin<ExtensionAware> {

    companion object {
        const val ID = "freecam.idea.sync"
        const val ICON_TASK_NAME = "ideaIconSync"
    }

    private val isIdeaSync: Boolean
        get() = System.getProperty("idea.sync.active", "false").toBoolean()

    override fun apply(target: ExtensionAware) = when (target) {
        is Project -> target.apply()
        is Settings -> target.apply()
        else -> error("The plugin may only be applied to settings and projects")
    }

    private fun Project.apply() {
        tasks.register<IdeaIconSyncTask>(ICON_TASK_NAME)
    }

    private fun Settings.apply() = with(gradle) {
        settingsEvaluated {
            // Apply plugin to the root project
            rootProject { plugins.apply(ID) }

            // Request task on idea sync
            if (isIdeaSync) {
                startParameter.taskRequests += IdeaSyncTaskExecutionRequest.of(rootProject, ICON_TASK_NAME)
            }
        }
    }
}
