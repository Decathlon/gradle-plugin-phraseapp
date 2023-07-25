package phraseapp

import org.gradle.api.tasks.TaskAction
import phrase.tasks.UploadTask

@Deprecated(
    message = "This task is deprecated, use UploadTask instead",
    replaceWith = ReplaceWith("tasks.UploadTask")
)
abstract class UploadTask : UploadTask() {
    @TaskAction
    fun uploadDeprecated() = kotlin.run {
        logger.warn("phraseappUpload is deprecated, use phraseUpload")
        upload()
    }
}