package phraseapp

import org.gradle.api.tasks.TaskAction
import phraseapp.tasks.UploadTask

@Deprecated("This task is deprecated, use tasks.UploadTask instead")
abstract class UploadTask : UploadTask() {
    @TaskAction
    fun uploadDeprecated() = kotlin.run {
        logger.warn("phraseappUpload is deprecated, use phraseUpload")
        upload()
    }
}