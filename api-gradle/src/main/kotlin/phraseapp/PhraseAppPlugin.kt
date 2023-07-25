package phraseapp

import org.gradle.api.Plugin
import org.gradle.api.Project
import phraseapp.internal.platforms.Flutter
import java.io.File
import phraseapp.ClearTranslationsTask as DeprecatedClearTranslationsTask
import phraseapp.DownloadTask as DeprecatedDownloadTask
import phraseapp.UploadTask as DeprecatedUploadTask

@Deprecated(
    message = "This plugin is deprecated and will be deleted in the next major version. Consider to use PhrasePlugin instead.",
    replaceWith = ReplaceWith("phrase.PhrasePlugin")
)
class PhraseAppPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            extensions.create("phraseapp", PhraseappPluginExtension::class.java)
            afterEvaluate {
                val phraseapp: PhraseappPluginExtension = extensions.getByType(PhraseappPluginExtension::class.java)
                val defaultOutput = "${buildDir.absolutePath}${File.separator}outputs${File.separator}phraseapp"
                val targetResFolders = getResFolders(phraseapp)

                tasks.create("phraseappDownload", DeprecatedDownloadTask::class.java).run {
                    baseUrl.set(phraseapp.phraseappBaseUrl.get())
                    projectId.set(phraseapp.projectId.get())
                    authToken.set(phraseapp.authToken.get())
                    resFolders.set(targetResFolders)
                    platform.set(phraseapp.platform.get().toNewPlatform())
                    output.set(phraseapp.outputLocation.getOrElse(defaultOutput))
                    overrideDefaultFile.set(phraseapp.overrideDefaultFile.get())
                    exceptions.set(phraseapp.exceptions.get())
                    placeholder.set(phraseapp.placeholder.get())
                    localeNameRegex.set(phraseapp.localeNameRegex.get())
                    ignoreComments.set(phraseapp.ignoreComments.get())
                    allowedLocaleCodes.set(phraseapp.allowedLocaleCodes.get())
                    description = "Download translations from the source set to PhraseApp"
                }

                tasks.create("phraseappUpload", DeprecatedUploadTask::class.java).run {
                    baseUrl.set(phraseapp.phraseappBaseUrl.get())
                    projectId.set(phraseapp.projectId.get())
                    authToken.set(phraseapp.authToken.get())
                    mainLocaleId.set(phraseapp.mainLocaleId.getOrElse(""))
                    platform.set(phraseapp.platform.get().toNewPlatform())
                    output.set(defaultOutput)
                    resFolders.set(targetResFolders)
                    description = "Upload default string file from the source set to PhraseApp"
                }

                tasks.create("phraseappClean", DeprecatedClearTranslationsTask::class.java).run {
                    platform.set(phraseapp.platform.get().toNewPlatform())
                    resFolders.set(targetResFolders)
                    description = "Clear all translations files in the target project"
                }

                tasks.create("phraseappCheck", CheckTask::class.java).run {
                    baseUrl.set(phraseapp.phraseappBaseUrl)
                    platform.set(phraseapp.platform.get().toNewPlatform())
                    projectId.set(phraseapp.projectId)
                    authToken.set(phraseapp.authToken)
                    localeNameRegex.set(phraseapp.localeNameRegex)
                    output.set(defaultOutput)
                    description = "This task checks plurals and placeholder only"
                }
            }
        }
    }

    private fun Project.getResFolders(phraseapp: PhraseappPluginExtension): Map<String, List<String>> = when {
        phraseapp.resFoldersMultiStrings.get().isNotEmpty() -> phraseapp.resFoldersMultiStrings.get()
        phraseapp.resFolders.get().isNotEmpty() -> phraseapp.resFolders.get()
            .map { "${projectDir.absolutePath}/$it" }
            .associateWith {
                when (phraseapp.platform.get().toNewPlatform()) {
                    is Flutter -> arrayListOf("strings_en.arb")
                    else -> arrayListOf("strings.xml")
                }
            }

        else -> arrayListOf(phraseapp.resFolder.get())
            .map { "${projectDir.absolutePath}/$it" }
            .associateWith {
                when (phraseapp.platform.get().toNewPlatform()) {
                    is Flutter -> arrayListOf("strings_en.arb")
                    else -> arrayListOf("strings.xml")
                }
            }
    }
}