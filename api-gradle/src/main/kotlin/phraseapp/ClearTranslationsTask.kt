package phraseapp

import org.gradle.api.tasks.TaskAction
import phrase.tasks.ClearTranslationsTask

@Deprecated(
    message = "This task is deprecated, use tasks.ClearTranslationsTask instead",
    replaceWith = ReplaceWith("tasks.ClearTranslationsTask")
)
abstract class ClearTranslationsTask : ClearTranslationsTask() {

    @TaskAction
    fun clearDeprecated() = run {
        logger.warn("phraseappClean is deprecated, use phraseClean")
        clear()
    }
}
