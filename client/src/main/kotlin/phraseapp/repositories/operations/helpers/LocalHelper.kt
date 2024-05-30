package phraseapp.repositories.operations.helpers

import phraseapp.extensions.ResourceTranslation
import phraseapp.extensions.parse
import phraseapp.internal.platforms.Platform
import phraseapp.internal.xml.PluralsTranslation
import phraseapp.internal.xml.StringTranslation
import phraseapp.internal.xml.StringsArrayTranslation
import phraseapp.repositories.operations.LanguageType
import phraseapp.repositories.operations.ResFolderType
import java.io.File

class LocalHelper(val platform: Platform) {
    /**
     * Merge all strings files in all res folders into one strings files.
     */
    fun mergeStringsFilesByResFolder(resFolders: Map<String, List<String>>): ResourceTranslation {
        val stringsFiles = getStringsFileByResFolder(resFolders)
        val resources: List<ResourceTranslation> = stringsFiles.map { it.value }
        return mergeResourceTranslations(resources)
    }

    /**
     * Get one XML DSL for all strings files in all resource folders.
     * @return Map with the res folder as key and the merge of all strings file in one XML DSL as value.
     */
    fun getStringsFileByResFolder(resFolders: Map<String, List<String>>)
            : Map<String, ResourceTranslation> = resFolders
        .map { it.key to getStringsFile(it.key, it.value) }
        .toMap()

    /**
     * Get resource translations by resource folder without any merge.
     * @return Map with the res folder as key and all strings file as value.
     */
    fun getResourceTranslationsByResFolder(
        resFolders: Map<String, List<String>>
    ): Map<String, List<ResourceTranslation>> =
        resFolders.map { it.key to getResourceTranslationList(it.key, it.value) }.toMap()

    /**
     * Get resources list from filenames list.
     * @return List of ResourceTranslation corresponding to the associated filename.
     */
    private fun getResourceTranslationList(
        resFolder: String, filenames: List<String>
    ): List<ResourceTranslation> =
        filenames
            .map { getResFolderFile(resFolder, it) }
            .map { it.readText().parse(it) }
            .toList()

    /**
     * Get XML DSL for all strings files in one resource folder.
     * @return merge of all keys for all strings files
     */
    private fun getStringsFile(resFolder: String, filenames: List<String>): ResourceTranslation {
        val resources: List<ResourceTranslation> = filenames
            .map { getResFolderFile(resFolder, it) }
            .map { it.readText().parse(it) }
            .toList()
        return mergeResourceTranslations(resources)
    }

    /**
     * Build resource folder file from res folder path, filename and the type of the res folder (default or locale).
     * @return File if exist or throw NoSuchFileException
     */
    private fun getResFolderFile(resFolder: String, filename: String): File {
        val value = platform.getResPath(LanguageType("", true))
        val file = File("${resFolder}${File.separator}${value}${File.separator}${filename}")
        if (file.exists().not()) throw NoSuchFileException(file)
        return file
    }

    private fun mergeResourceTranslations(resources: List<ResourceTranslation>): ResourceTranslation {
        val strings = arrayListOf<StringTranslation>()
        val plurals = arrayListOf<PluralsTranslation>()
        val arrays = arrayListOf<StringsArrayTranslation>()

        resources.forEach {
            strings.addAll(it.strings)
            plurals.addAll(it.plurals)
            arrays.addAll(it.arrays)
        }

        return ResourceTranslation(
            strings.toList().distinctBy { it.key },
            plurals.toList().distinctBy { it.key },
            arrays.toList().distinctBy { it.key }
        )
    }
}
