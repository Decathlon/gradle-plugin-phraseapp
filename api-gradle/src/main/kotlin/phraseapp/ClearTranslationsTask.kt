package phraseapp

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.repositories.operations.Cleaner

open class ClearTranslationsTask : DefaultTask() {
    lateinit var platform: Platform
    lateinit var resFolders: Map<String, List<String>>

    @TaskAction
    fun clear() {
        var throwable: Throwable? = null
        Cleaner(platform, FileOperationImpl()).clean(resFolders).subscribe({
            logger.info("All resources have been deleted!")
        }, {
            throwable = it
        })
        if (throwable != null) {
            throw GradleException("Something wrong happened during the cleaning...", throwable!!)
        }
    }
}