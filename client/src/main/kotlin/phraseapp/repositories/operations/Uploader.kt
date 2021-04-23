package phraseapp.repositories.operations

import io.reactivex.Completable
import io.reactivex.Single
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.repositories.operations.helpers.LocalHelper
import phraseapp.repositories.operations.helpers.PrinterHelper

class Uploader(platform: Platform, buildDir: String, fileOperation: FileOperation, private val network: PhraseAppNetworkDataSource) {
    private val localHelper = LocalHelper(platform)
    private val printerHelper = PrinterHelper(platform, buildDir, fileOperation)

    fun upload(localeId: String, resFolders: Map<String, List<String>>): Completable {
        return Single.just(localeId).doOnSuccess {
            val stringsFile = localHelper.mergeStringsFilesByResFolder(resFolders)
            printerHelper.printTempStringsFile(stringsFile.toResource())
        }.flatMapCompletable { network.upload(localeId, printerHelper.tempStringFilePath) }
    }
}