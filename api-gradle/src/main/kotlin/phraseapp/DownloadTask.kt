package phraseapp

import org.gradle.api.tasks.TaskAction
import phrase.tasks.DownloadTask

@Deprecated(
    message = "This task is deprecated, use DownloadTask instead",
    replaceWith = ReplaceWith("tasks.DownloadTask")
)
abstract class DownloadTask : DownloadTask() {
    @TaskAction
    fun downloadDeprecated() = run {
        logger.warn("phraseappDownload is deprecated, use phraseDownload")
        download()
    }
}
