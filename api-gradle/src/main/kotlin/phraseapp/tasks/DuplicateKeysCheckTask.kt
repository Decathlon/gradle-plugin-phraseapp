package phraseapp.tasks

import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import phraseapp.internal.platforms.Platform
import phraseapp.repositories.checks.DuplicateKeysRepository

abstract class DuplicateKeysCheckTask : DefaultTask() {

    @get:Input
    abstract val resFolders: MapProperty<String, List<String>>

    @get:Input
    abstract val platform: Property<Platform>

    @TaskAction
    fun checkDuplicateKeys() = runBlocking {
        try {
            DuplicateKeysRepository(platform.get())
                .check(resFolders.get())
            logger.info("There is no duplicated key")
        } catch (error: Throwable) {
            throw GradleException("Duplicated key found", error)
        }
    }
}