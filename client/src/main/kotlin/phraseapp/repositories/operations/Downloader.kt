package phraseapp.repositories.operations

import kotlinx.coroutines.coroutineScope
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.xml.Resource
import phraseapp.network.DEFAULT_ALLOWED_LOCALE_CODES
import phraseapp.network.DEFAULT_EXCEPTIONS
import phraseapp.network.DEFAULT_OVERRIDE_DEFAULT_FILE
import phraseapp.network.DEFAULT_PLACEHOLDER
import phraseapp.network.DEFAULT_REGEX
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.parsers.xml.DEFAULT_IGNORE_COMMENTS
import phraseapp.repositories.operations.helpers.LocalHelper
import phraseapp.repositories.operations.helpers.PrinterHelper
import phraseapp.repositories.operations.helpers.ReducerHelper

class Downloader(
    platform: Platform,
    buildDir: String,
    fileOperation: FileOperation,
    private val network: PhraseAppNetworkDataSource,
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
        ignoreComments: Boolean = DEFAULT_IGNORE_COMMENTS,
        allowedLocaleCodes: List<String> = DEFAULT_ALLOWED_LOCALE_CODES,
    ) = coroutineScope {
        val strings = localHelper.getStringsFileByResFolder(resFolders = resFolders)
        val locales = network.downloadAllLocales(
            exceptions = exceptions,
            placeHolder = placeholder,
            localeNameRegex = localeNameRegex,
            allowedLocaleCodes = allowedLocaleCodes
        )
        val resources = reducerHelper.reduceKeysForAllStringsFilesAndForAllLocales(
            strings = strings,
            remoteStrings = locales,
            ignoreComments = ignoreComments
        )
        printerHelper.printResources(
            configurations = resources,
            overrideDefaultFile = overrideDefaultFile
        )
        printerHelper.printLocales(types = getTypes(resources))

        return@coroutineScope resources
    }

    private fun getTypes(configurations: Map<String, Map<ResFolderType, Resource>>): List<ResFolderType> =
        configurations.entries.first().value.keys.toList()
}

sealed class ResFolderType(open val language: String, val isDefault: Boolean)
class LanguageType(language: String, isDefault: Boolean) : ResFolderType(language, isDefault)
class LocaleType(language: String, val country: String, isDefault: Boolean) :
    ResFolderType(language, isDefault)
