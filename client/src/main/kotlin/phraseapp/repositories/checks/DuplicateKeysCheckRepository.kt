package phraseapp.repositories.checks

import kotlinx.coroutines.coroutineScope
import phraseapp.internal.exception.DuplicateKeyException
import phraseapp.internal.platforms.Platform
import phraseapp.internal.xml.PluralsTranslation
import phraseapp.internal.xml.StringTranslation
import phraseapp.internal.xml.StringsArrayTranslation
import phraseapp.internal.xml.StringsTranslationNode
import phraseapp.repositories.operations.helpers.LocalHelper

class DuplicateKeysCheckRepository(platform: Platform) {
    private val localHelper = LocalHelper(platform)

    suspend fun check(resFolders: Map<String, List<String>>) = coroutineScope {
        val resources = localHelper.getResourceTranslationsByResFolder(resFolders).map { it.value }
        val strings = arrayListOf<StringTranslation>()
        val plurals = arrayListOf<PluralsTranslation>()
        val arrays = arrayListOf<StringsArrayTranslation>()
        resources.forEach { resourcesFile ->
            resourcesFile.forEach {
                strings.addAll(it.strings)
                plurals.addAll(it.plurals)
                arrays.addAll(it.arrays)
            }
        }
        strings.filter { it.translatable }.checkDuplicateKeys()
        plurals.filter { it.translatable }.checkDuplicateKeys()
        arrays.filter { it.translatable }.checkDuplicateKeys()
    }

    private fun List<StringsTranslationNode>.checkDuplicateKeys() {
        if (distinctBy { it.key }.size == size) return
        val redundantKeys = map { it.key }
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        val redundant = redundantKeys.map { key -> filter { it.key == key } }
        throw DuplicateKeyException(redundant)
    }
}
