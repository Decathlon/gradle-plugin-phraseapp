package phraseapp.repositories.operations

import io.reactivex.Completable
import io.reactivex.Single
import phraseapp.extensions.ResourceTranslation
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.xml.Resource
import phraseapp.network.*
import phraseapp.repositories.operations.helpers.LocalHelper
import phraseapp.repositories.operations.helpers.PrinterHelper
import phraseapp.repositories.operations.helpers.ReducerHelper

class Downloader(platform: Platform, buildDir: String, fileOperation: FileOperation, private val network: PhraseAppNetworkDataSource) {
    private val localHelper = LocalHelper(platform)
    private val reducerHelper = ReducerHelper(platform)
    private val printerHelper = PrinterHelper(platform, buildDir, fileOperation)

    fun download(
            resFolders: Map<String, List<String>>,
            overrideDefaultFile: Boolean = DEFAULT_OVERRIDE_DEFAULT_FILE,
            exceptions: Map<String, String> = DEFAULT_EXCEPTIONS,
            placeholder: Boolean = DEFAULT_PLACEHOLDER,
            localeNameRegex: String = DEFAULT_REGEX): Completable {
        return Single.just(localHelper.getStringsFileByResFolder(resFolders)).flatMap { strings: Map<String, ResourceTranslation> ->
            return@flatMap network.downloadAllLocales(overrideDefaultFile, exceptions, placeholder, localeNameRegex).map { locales: Map<String, LocaleContent> ->
                return@map reducerHelper.reduceKeysForAllStringsFilesAndForAllLocales(strings, locales)
            }
        }.doOnSuccess {
            printerHelper.printResources(it)
            printerHelper.printLocales(getTypes(it))
        }.ignoreElement()
    }

    private fun getTypes(configurations: Map<String, Map<ResFolderType, Resource>>): List<ResFolderType> =
            configurations.entries.first().value.keys.toList()
}

sealed class ResFolderType
object DefaultType : ResFolderType()
class LanguageType(val language: String) : ResFolderType()
class LocaleType(val language: String, val country: String) : ResFolderType()
