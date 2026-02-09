package phrase

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import phraseapp.internal.platforms.Flutter
import phrase.tasks.ClearTranslationsTask
import phrase.tasks.DownloadTask
import phrase.tasks.DuplicateKeysCheckTask
import phrase.tasks.PlaceHolderCheckTask
import phrase.tasks.PluralsCheckTask
import phrase.tasks.UploadTask
import java.io.File

class PhrasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            extensions.create("phrase", PhraseExtension::class.java)
            afterEvaluate {
                val phrase: PhraseExtension = extensions.getByType(PhraseExtension::class.java)
                val defaultOutput =
                    "${project.layout.buildDirectory.get().asFile}${File.separator}outputs${File.separator}phrase"
                val targetResFolders = getResFolders(phrase)

                tasks.register("phraseDownload", DownloadTask::class.java) { task ->
                    task.baseUrl.set(phrase.phraseBaseUrl.get())
                    task.projectId.set(phrase.projectId.get())
                    task.authToken.set(phrase.authToken.get())
                    task.resFolders.set(targetResFolders)
                    task.platform.set(phrase.platform.get().toNewPlatform())
                    task.output.set(phrase.outputLocation.getOrElse(defaultOutput))
                    task.overrideDefaultFile.set(phrase.overrideDefaultFile.get())
                    task.exceptions.set(phrase.exceptions.get())
                    task.placeholder.set(phrase.placeholder.get())
                    task.localeNameRegex.set(phrase.localeNameRegex.get())
                    task.ignoreComments.set(phrase.ignoreComments.get())
                    task.allowedLocaleCodes.set(phrase.allowedLocaleCodes.get())
                    task.description = "Download translations from the source set to PhraseApp"
                }

                tasks.register("phraseUpload", UploadTask::class.java) { task ->
                    task.baseUrl.set(phrase.phraseBaseUrl.get())
                    task.projectId.set(phrase.projectId.get())
                    task.authToken.set(phrase.authToken.get())
                    task.mainLocaleId.set(phrase.mainLocaleId.getOrElse(""))
                    task.platform.set(phrase.platform.get().toNewPlatform())
                    task.output.set(defaultOutput)
                    task.resFolders.set(targetResFolders)
                    task.description = "Upload default string file from the source set to PhraseApp"
                }

                tasks.register("phraseClean", ClearTranslationsTask::class.java) { task ->
                    task.platform.set(phrase.platform.get().toNewPlatform())
                    task.resFolders.set(targetResFolders)
                    task.description = "Clear all translations files in the target project"
                }

                val duplicateKeysCheckTask = tasks.register("phraseDuplicateKeysCheck", DuplicateKeysCheckTask::class.java) { task ->
                    task.platform.set(phrase.platform.get().toNewPlatform())
                    task.resFolders.set(targetResFolders)
                    task.description = "Check if there are duplicate keys in res folder"
                }

                val pluralsCheckTask = tasks.register("phrasePluralsCheck", PluralsCheckTask::class.java) { task ->
                    task.baseUrl.set(phrase.phraseBaseUrl)
                    task.platform.set(phrase.platform.get().toNewPlatform())
                    task.projectId.set(phrase.projectId)
                    task.authToken.set(phrase.authToken)
                    task.localeNameRegex.set(phrase.localeNameRegex)
                    task.output.set(defaultOutput)
                    task.description = "Check plural strings are well formatted"
                }

                val placeHolderCheckTask = tasks.register("phrasePlaceHolderCheck", PlaceHolderCheckTask::class.java) { task ->
                    task.baseUrl.set(phrase.phraseBaseUrl)
                    task.platform.set(phrase.platform.get().toNewPlatform())
                    task.projectId.set(phrase.projectId)
                    task.authToken.set(phrase.authToken)
                    task.localeNameRegex.set(phrase.localeNameRegex)
                    task.output.set(defaultOutput)
                    task.description = "Check placeholders aren't removed in other locale string resource folders"
                }

                tasks.register("phraseCheck") { task ->
                    task.dependsOn(duplicateKeysCheckTask)
                    task.dependsOn(pluralsCheckTask)
                    task.dependsOn(placeHolderCheckTask)
                    task.description = "Apply all phrase checks"
                }
            }
        }
    }

    private fun Project.getResFolders(phrase: PhraseExtension): Map<String, List<String>> =
        when {
            phrase.resFoldersMultiStrings.get()
                .isNotEmpty() -> phrase.resFoldersMultiStrings.get()

            phrase.resFolders.get().isNotEmpty() -> phrase.resFolders.get()
                .map { "${projectDir.absolutePath}/$it" }
                .associateWith {
                    when (phrase.platform.get().toNewPlatform()) {
                        is Flutter -> arrayListOf("strings_en.arb")
                        else -> arrayListOf("strings.xml")
                    }
                }

            else -> throw GradleException("Please, configure resFolders property")
        }
}