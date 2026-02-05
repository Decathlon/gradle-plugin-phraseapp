package phrase.tasks

import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.repositories.operations.Downloader

abstract class DownloadTask : DefaultTask() {
    @get:Input
    abstract val baseUrl: Property<String>

    @get:Input
    abstract val authToken: Property<String>

    @get:Input
    abstract val projectId: Property<String>

    @get:Input
    abstract val resFolders: MapProperty<String, List<String>>

    @get:Input
    abstract val platform: Property<Platform>

    @get:Input
    abstract val output: Property<String>

    @get:Input
    abstract val localeNameRegex: Property<String>

    @get:Input
    abstract val overrideDefaultFile: Property<Boolean>

    @get:Input
    abstract val exceptions: MapProperty<String, String>

    @get:Input
    abstract val placeholder: Property<Boolean>

    @get:Input
    abstract val ignoreComments: Property<Boolean>

    @get:Input
    abstract val allowedLocaleCodes: ListProperty<String>

    init {
        overrideDefaultFile.convention(false)
        exceptions.convention(emptyMap())
        placeholder.convention(false)
        ignoreComments.convention(false)
        allowedLocaleCodes.convention(emptyList())
    }

    @TaskAction
    fun download() = runBlocking {
        try {
            println("[PhraseAppNetwork] Starting download task...")
            val network = PhraseAppNetworkDataSource.newInstance(
                baseUrl.get(),
                authToken.get(),
                projectId.get(),
                platform.get().format
            )
            val fileOperation: FileOperation = FileOperationImpl()
            Downloader(platform.get(), output.get(), fileOperation, network)
                .download(
                    resFolders.get(),
                    overrideDefaultFile.get(),
                    exceptions.get(),
                    placeholder.get(),
                    localeNameRegex.get(),
                    ignoreComments.get(),
                    allowedLocaleCodes.get()
                )
            logger.info("All resources have been printed!")
        } catch (error: Throwable) {
            throw GradleException("Something wrong happened during the downloading", error)
        }
    }
}