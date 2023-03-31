package phraseapp.repositories.operations.helpers

import phraseapp.extensions.ResourceTranslation
import phraseapp.extensions.parse
import phraseapp.internal.platforms.Platform
import phraseapp.internal.xml.Resource
import phraseapp.network.LocaleContent
import phraseapp.repositories.operations.DefaultType
import phraseapp.repositories.operations.LanguageType
import phraseapp.repositories.operations.LocaleType
import phraseapp.repositories.operations.ResFolderType

class ReducerHelper(val platform: Platform) {
    /**
     * Reduces keys for all strings files and for all locales.
     * @return Map with the target res folder as key and the reduced keys for all remote locales.
     */
    fun reduceKeysForAllStringsFilesAndForAllLocales(
        strings: Map<String, ResourceTranslation>,
        remoteStrings: Map<String, LocaleContent>,
        ignoreComments: Boolean
    ): Map<String, Map<ResFolderType, Resource>> =
        strings.map {
            it.key to reduceKeysForAllLocales(it.value, remoteStrings, ignoreComments)
        }.toMap()

    /**
     * Reduces keys for all remote locales.
     * @return Map with the res folder type as key and the resource with reduced keys as value.
     */
    private fun reduceKeysForAllLocales(
        stringsFile: ResourceTranslation,
        remoteStrings: Map<String, LocaleContent>,
        ignoreComments: Boolean
    ): Map<ResFolderType, Resource> {
        val keys: Set<String> = stringsFile.strings.map { it.key }.union(stringsFile.plurals.map { it.key })
        return remoteStrings.map {
            val type = if (it.value.isDefault) DefaultType
            else if (it.key.split("-").size > 1) LocaleType(it.key.split("-")[0], it.key.split("-")[1])
            else LanguageType(it.key)
            val resource = it.value.content.parse(platform.format, ignoreComments)
            return@map type to reduceKeys(keys, resource)
        }.toMap()
    }

    /**
     * Reduce keys from content with keys by intersection.
     * @return Resource where we have all reduced keys.
     */
    private fun reduceKeys(keys: Set<String>, resource: ResourceTranslation): Resource {
        val remoteKeys = resource.strings.map { it.key }.union(resource.plurals.map { it.key })
        val intersect = keys.intersect(remoteKeys)
        return Resource(
            resource.strings.filter { intersect.contains(it.key) }
                    + resource.plurals.filter { intersect.contains(it.key) }
        )
    }
}