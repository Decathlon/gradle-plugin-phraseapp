package phraseapp

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.repositories.operations.Uploader

abstract class UploadTask : DefaultTask() {
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
    abstract val mainLocaleId: Property<String>

    @TaskAction
    fun upload() {
        var throwable: Throwable? = null
        val network = PhraseAppNetworkDataSource.newInstance(baseUrl.get(), authToken.get(), projectId.get(), platform.get().format)
        val fileOperation: FileOperation = FileOperationImpl()
        Uploader(platform.get(), output.get(), fileOperation, network).upload(mainLocaleId.get(), resFolders.get()).subscribe({
            logger.info("All string have been uploaded!")
        }, {
            throwable = it
        })
        if (throwable != null) {
            throw GradleException("Something wrong happened during the uploading", throwable!!)
        }
    }
}