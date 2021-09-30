package phraseapp

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.repositories.operations.Cleaner

abstract class ClearTranslationsTask : DefaultTask() {
    @get:Input
    abstract val platform: Property<Platform>
    @get:Input
    abstract val resFolders: Property<Map<String, List<String>>>

    @TaskAction
    fun clear() {
        var throwable: Throwable? = null
        Cleaner(platform.get(), FileOperationImpl()).clean(resFolders.get()).subscribe({
            logger.info("All resources have been deleted!")
        }, {
            throwable = it
        })
        if (throwable != null) {
            throw GradleException("Something wrong happened during the cleaning...", throwable!!)
        }
    }
}