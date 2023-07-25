package phraseapp.tasks

import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import phraseapp.internal.platforms.Platform
import phraseapp.repositories.checks.CheckRepository
import phraseapp.repositories.checks.CheckType.PLACEHOLDER

abstract class PlaceHolderCheckTask : DefaultTask() {
    @get:Input
    abstract val baseUrl: Property<String>

    @get:Input
    abstract val platform: Property<Platform>

    @get:Input
    abstract val authToken: Property<String>

    @get:Input
    abstract val projectId: Property<String>

    @get:Input
    abstract val localeNameRegex: Property<String>

    @get:Input
    abstract val output: Property<String>

    @TaskAction
    fun action() = runBlocking {
        try {
            CheckRepository
                .newInstance(
                    baseUrl.get(),
                    output.get(),
                    localeNameRegex.get(),
                    authToken.get(),
                    projectId.get(),
                    platform.get()
                ).check(listOf(PLACEHOLDER))

            logger.info("You don't have any placeholder error in your translations!")
        } catch (error: Throwable) {
            throw GradleException("You have placeholder errors in your translations", error)
        }
    }
}