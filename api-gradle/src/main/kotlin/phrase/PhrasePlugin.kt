package phrase

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
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
                    "${buildDir.absolutePath}${File.separator}outputs${File.separator}phrase"
                val targetResFolders = getResFolders(phrase)

                tasks.create("phraseDownload", DownloadTask::class.java).run {
                    baseUrl.set(phrase.phraseBaseUrl.get())
                    projectId.set(phrase.projectId.get())
                    authToken.set(phrase.authToken.get())
                    resFolders.set(targetResFolders)
                    platform.set(phrase.platform.get().toNewPlatform())
                    output.set(phrase.outputLocation.getOrElse(defaultOutput))
                    overrideDefaultFile.set(phrase.overrideDefaultFile.get())
                    exceptions.set(phrase.exceptions.get())
                    placeholder.set(phrase.placeholder.get())
                    localeNameRegex.set(phrase.localeNameRegex.get())
                    ignoreComments.set(phrase.ignoreComments.get())
                    allowedLocaleCodes.set(phrase.allowedLocaleCodes.get())
                    description = "Download translations from the source set to PhraseApp"
                }

                tasks.create("phraseUpload", UploadTask::class.java).run {
                    baseUrl.set(phrase.phraseBaseUrl.get())
                    projectId.set(phrase.projectId.get())
                    authToken.set(phrase.authToken.get())
                    mainLocaleId.set(phrase.mainLocaleId.getOrElse(""))
                    platform.set(phrase.platform.get().toNewPlatform())
                    output.set(defaultOutput)
                    resFolders.set(targetResFolders)
                    description = "Upload default string file from the source set to PhraseApp"
                }

                tasks.create("phraseClean", ClearTranslationsTask::class.java).run {
                    platform.set(phrase.platform.get().toNewPlatform())
                    resFolders.set(targetResFolders)
                    description = "Clear all translations files in the target project"
                }

                tasks.create("phraseDuplicateKeysCheck", DuplicateKeysCheckTask::class.java).run {
                    platform.set(phrase.platform.get().toNewPlatform())
                    resFolders.set(targetResFolders)
                    description = "Check if there are duplicate keys in res folder"
                }

                tasks.create("phrasePluralsCheck", PluralsCheckTask::class.java).run {
                    baseUrl.set(phrase.phraseBaseUrl)
                    platform.set(phrase.platform.get().toNewPlatform())
                    projectId.set(phrase.projectId)
                    authToken.set(phrase.authToken)
                    localeNameRegex.set(phrase.localeNameRegex)
                    output.set(defaultOutput)
                    description = "Check plural strings are well formatted"
                }

                tasks.create("phrasePlaceHolderCheck", PlaceHolderCheckTask::class.java).run {
                    baseUrl.set(phrase.phraseBaseUrl)
                    platform.set(phrase.platform.get().toNewPlatform())
                    projectId.set(phrase.projectId)
                    authToken.set(phrase.authToken)
                    localeNameRegex.set(phrase.localeNameRegex)
                    output.set(defaultOutput)
                    description =
                        "Check placeholders aren't removed in other locale string resource folders"
                }

                tasks.create("phraseCheck").run {
                    dependsOn("phraseDuplicateKeysCheck")
                    dependsOn("phrasePluralsCheck")
                    dependsOn("phrasePlaceHolderCheck")
                    description = "Apply all phrase checks"
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