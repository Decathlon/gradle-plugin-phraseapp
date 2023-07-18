package phraseapp.repositories.operations.helpers

import phraseapp.extensions.ResourceTranslation
import phraseapp.extensions.parse
import phraseapp.internal.exception.DuplicateKeyException
import phraseapp.internal.platforms.Platform
import phraseapp.internal.xml.PluralsTranslation
import phraseapp.internal.xml.StringTranslation
import phraseapp.internal.xml.StringsArrayTranslation
import phraseapp.internal.xml.StringsTranslationNode
import phraseapp.repositories.operations.DefaultType
import phraseapp.repositories.operations.ResFolderType
import java.io.File

class LocalHelper(val platform: Platform) {
    /**
     * Merge all strings files in all res folders into one strings files.
     */
    fun mergeStringsFilesByResFolder(
        resFolders: Map<String, List<String>>,
        checkDuplicateKeys: Boolean = false
    ): ResourceTranslation {
        val stringsFiles = getStringsFileByResFolder(resFolders, checkDuplicateKeys)
        val resources: List<ResourceTranslation> = stringsFiles.map { it.value }
        return mergeResourceTranslations(resources, checkDuplicateKeys)
    }

    /**
     * Get one XML DSL for all strings files in all resource folders.
     * @return Map with the res folder as key and the merge of all strings file in one XML DSL as value.
     */
    fun getStringsFileByResFolder(
        resFolders: Map<String, List<String>>,
        checkDuplicateKeys: Boolean = false
    ): Map<String, ResourceTranslation> = resFolders
        .map { it.key to getStringsFile(it.key, it.value, checkDuplicateKeys) }
        .toMap()

    /**
     * Get XML DSL for all strings files in one resource folder.
     * @return merge of all keys for all strings files
     */
    private fun getStringsFile(
        resFolder: String,
        filenames: List<String>,
        checkDuplicateKeys: Boolean
    ): ResourceTranslation {
        val resources: List<ResourceTranslation> = filenames
            .map { getResFolderFile(resFolder, it, DefaultType) }
            .map { it.readText().parse(it) }
            .toList()
        return mergeResourceTranslations(resources, checkDuplicateKeys)
    }

    /**
     * Build resource folder file from res folder path, filename and the type of the res folder (default or locale).
     * @return File if exist or throw NoSuchFileException
     */
    private fun getResFolderFile(resFolder: String, filename: String, type: ResFolderType): File {
        val value = platform.getResPath(type)
        val file = File("${resFolder}${File.separator}${value}${File.separator}${filename}")
        if (file.exists().not()) throw NoSuchFileException(file)
        return file
    }

    private fun mergeResourceTranslations(
        resources: List<ResourceTranslation>,
        checkDuplicateKeys: Boolean
    ): ResourceTranslation {
        val strings = arrayListOf<StringTranslation>()
        val plurals = arrayListOf<PluralsTranslation>()
        val arrays = arrayListOf<StringsArrayTranslation>()

        resources.forEach {
            strings.addAll(it.strings)
            plurals.addAll(it.plurals)
            arrays.addAll(it.arrays)
        }

        if (checkDuplicateKeys) {
            strings.filter { it.translatable }.checkDuplicateKeys()
            plurals.filter { it.translatable }.checkDuplicateKeys()
            arrays.filter { it.translatable }.checkDuplicateKeys()
        }

        return ResourceTranslation(
            strings.toList().distinctBy { it.key },
            plurals.toList().distinctBy { it.key },
            arrays.toList().distinctBy { it.key }
        )
    }

    private fun List<StringsTranslationNode>.checkDuplicateKeys() {
        if (distinctBy { it.key }.size != size) {
            val redundantKeys =
                map { it.key }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

            val redundant = redundantKeys.map { key ->
                filter { it.key == key }
            }
            throw DuplicateKeyException(redundant)
        }
    }
}