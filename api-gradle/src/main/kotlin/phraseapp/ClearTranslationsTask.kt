package phraseapp

import org.gradle.api.tasks.TaskAction
import phraseapp.tasks.ClearTranslationsTask

@Deprecated("This task is deprecated, use tasks.ClearTranslationsTask instead")
abstract class ClearTranslationsTask : ClearTranslationsTask() {

    @TaskAction
    fun clearDeprecated() = run {
        logger.warn("phraseappClean is deprecated, use phraseClean")
        clear()
    }
}