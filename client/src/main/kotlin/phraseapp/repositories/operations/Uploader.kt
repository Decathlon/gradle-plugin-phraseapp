package phraseapp.repositories.operations

import kotlinx.coroutines.coroutineScope
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.repositories.operations.helpers.LocalHelper
import phraseapp.repositories.operations.helpers.PrinterHelper

class Uploader(platform: Platform, buildDir: String, fileOperation: FileOperation, private val network: PhraseAppNetworkDataSource) {
    private val localHelper = LocalHelper(platform)
    private val printerHelper = PrinterHelper(platform, buildDir, fileOperation)

    suspend fun upload(localeId: String, resFolders: Map<String, List<String>>) = coroutineScope {
        val stringsFile = localHelper.mergeStringsFilesByResFolder(resFolders)
        printerHelper.printTempStringsFile(stringsFile.toResource())
        return@coroutineScope network.upload(localeId, printerHelper.tempStringFilePath)
    }
}