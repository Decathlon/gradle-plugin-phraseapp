package phraseapp

import org.gradle.api.tasks.TaskAction
import phraseapp.tasks.DownloadTask

@Deprecated("This task is deprecated, use tasks.DownloadTask instead")
abstract class DownloadTask : DownloadTask() {
    @TaskAction
    fun downloadDeprecated() = run {
        logger.warn("phraseappDownload is deprecated, use phraseDownload")
        download()
    }
}