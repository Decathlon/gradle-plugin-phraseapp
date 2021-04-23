package phraseapp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import phraseapp.internal.platforms.Flutter
import java.io.File

class PhraseAppPlugin : Plugin<Project> {
    override fun apply(project: Project?) {
        if (project == null) {
            throw ProjectConfigurationException("The plugin can't be applied to your project.", Throwable())
        }
        project.run {
            extensions.create("phraseapp", PhraseappPluginExtension::class.java)
            afterEvaluate {
                val phraseapp: PhraseappPluginExtension = extensions.getByType(PhraseappPluginExtension::class.java)
                val defaultOutput = "${buildDir.absolutePath}${File.separator}outputs${File.separator}phraseapp"
                val targetResFolders = getResFolders(phraseapp)

                tasks.create("phraseappDownload", DownloadTask::class.java).run {
                    baseUrl = phraseapp.phraseappBaseUrl
                    projectId = phraseapp.projectId
                    authToken = phraseapp.authToken
                    resFolders = targetResFolders
                    platform = phraseapp.platform.toNewPlatform()
                    output = phraseapp.outputLocation ?: defaultOutput
                    overrideDefaultFile = phraseapp.overrideDefaultFile
                    exceptions = phraseapp.exceptions
                    placeholder = phraseapp.placeholder
                    localeNameRegex = phraseapp.localeNameRegex
                    description = "Download translations from the source set to PhraseApp"
                }

                tasks.create("phraseappUpload", UploadTask::class.java).run {
                    baseUrl = phraseapp.phraseappBaseUrl
                    projectId = phraseapp.projectId
                    authToken = phraseapp.authToken
                    mainLocaleId = phraseapp.mainLocaleId!!
                    platform = phraseapp.platform.toNewPlatform()
                    output = defaultOutput
                    resFolders = targetResFolders
                    description = "Upload default string file from the source set to PhraseApp"
                }

                tasks.create("phraseappClean", ClearTranslationsTask::class.java).run {
                    platform = phraseapp.platform.toNewPlatform()
                    resFolders = targetResFolders
                    description = "Clear all translations files in the target project"
                }

                tasks.create("phraseappCheck", CheckTask::class.java).run {
                    baseUrl = phraseapp.phraseappBaseUrl
                    platform = phraseapp.platform.toNewPlatform()
                    projectId = phraseapp.projectId
                    authToken = phraseapp.authToken
                    localeNameRegex = phraseapp.localeNameRegex
                    output = defaultOutput
                }
            }
        }
    }

    private fun Project.getResFolders(phraseapp: PhraseappPluginExtension): Map<String, List<String>> = when {
        phraseapp.resFoldersMultiStrings.isNotEmpty() -> phraseapp.resFoldersMultiStrings
        phraseapp.resFolders.isNotEmpty() -> phraseapp.resFolders.associateWith {
            when (phraseapp.platform.toNewPlatform()) {
                is Flutter -> arrayListOf("strings_en.arb")
                else -> arrayListOf("strings.xml")
            }

        }
        else -> arrayListOf(phraseapp.resFolder)
                .map { "${projectDir.absolutePath}/$it" }
                .associateWith {
                    when (phraseapp.platform.toNewPlatform()) {
                        is Flutter -> arrayListOf("strings_en.arb")
                        else -> arrayListOf("strings.xml")
                    }
                }
    }
}