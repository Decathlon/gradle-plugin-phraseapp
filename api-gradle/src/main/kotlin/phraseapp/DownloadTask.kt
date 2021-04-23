package phraseapp

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.repositories.operations.Downloader

open class DownloadTask : DefaultTask() {
    lateinit var baseUrl: String
    lateinit var authToken: String
    lateinit var projectId: String
    lateinit var resFolders: Map<String, List<String>>
    lateinit var platform: Platform
    lateinit var output: String
    lateinit var localeNameRegex: String
    var overrideDefaultFile: Boolean = false
    var exceptions: Map<String, String> = emptyMap()
    var placeholder: Boolean = false

    @TaskAction
    fun download() {
        var throwable: Throwable? = null
        val network = PhraseAppNetworkDataSource.newInstance(baseUrl, authToken, projectId, platform.format)
        val fileOperation: FileOperation = FileOperationImpl()

        Downloader(platform, output, fileOperation, network).download(resFolders, overrideDefaultFile, exceptions, placeholder, localeNameRegex).subscribe({
            logger.info("All resources have been printed!")
        }, {
            throwable = it
        })
        if (throwable != null) {
            throw GradleException("Something wrong happened during the downloading", throwable!!)
        }
    }
}