package phraseapp.repositories.operations

import kotlinx.coroutines.coroutineScope
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.xml.Resource
import phraseapp.network.*
import phraseapp.parsers.xml.DEFAULT_IGNORE_COMMENTS
import phraseapp.repositories.operations.helpers.LocalHelper
import phraseapp.repositories.operations.helpers.PrinterHelper
import phraseapp.repositories.operations.helpers.ReducerHelper

class Downloader(
    platform: Platform,
    buildDir: String,
    fileOperation: FileOperation,
    private val network: PhraseAppNetworkDataSource
) {
    private val localHelper = LocalHelper(platform)
    private val reducerHelper = ReducerHelper(platform)
    private val printerHelper = PrinterHelper(platform, buildDir, fileOperation)

    suspend fun download(
        resFolders: Map<String, List<String>>,
        overrideDefaultFile: Boolean = DEFAULT_OVERRIDE_DEFAULT_FILE,
        exceptions: Map<String, String> = DEFAULT_EXCEPTIONS,
        placeholder: Boolean = DEFAULT_PLACEHOLDER,
        localeNameRegex: String = DEFAULT_REGEX,
        ignoreComments: Boolean = DEFAULT_IGNORE_COMMENTS
    ) = coroutineScope {
        val strings = localHelper.getStringsFileByResFolder(resFolders)
        val locales = network.downloadAllLocales(overrideDefaultFile, exceptions, placeholder, localeNameRegex)
        val resources = reducerHelper.reduceKeysForAllStringsFilesAndForAllLocales(strings, locales, ignoreComments)
        printerHelper.printResources(resources)
        printerHelper.printLocales(getTypes(resources))
        return@coroutineScope resources
    }

    private fun getTypes(configurations: Map<String, Map<ResFolderType, Resource>>): List<ResFolderType> =
        configurations.entries.first().value.keys.toList()
}

sealed class ResFolderType
object DefaultType : ResFolderType()
class LanguageType(val language: String) : ResFolderType()
class LocaleType(val language: String, val country: String) : ResFolderType()
