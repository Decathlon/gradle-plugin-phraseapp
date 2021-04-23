package phraseapp

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.repositories.operations.Uploader

open class UploadTask : DefaultTask() {
    lateinit var baseUrl: String
    lateinit var authToken: String
    lateinit var projectId: String
    lateinit var resFolders: Map<String, List<String>>
    lateinit var platform: Platform
    lateinit var output: String
    lateinit var mainLocaleId: String

    @TaskAction
    fun upload() {
        var throwable: Throwable? = null
        val network = PhraseAppNetworkDataSource.newInstance(baseUrl, authToken, projectId, platform.format)
        val fileOperation: FileOperation = FileOperationImpl()
        Uploader(platform, output, fileOperation, network).upload(mainLocaleId, resFolders).subscribe({
            logger.info("All string have been uploaded!")
        }, {
            throwable = it
        })
        if (throwable != null) {
            throw GradleException("Something wrong happened during the uploading", throwable!!)
        }
    }
}