package phraseapp.repositories.operations

import io.reactivex.Completable
import io.reactivex.Observable
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation

class Cleaner(val platform: Platform, val fileOperation: FileOperation) {
    fun clean(resFolders: Map<String, List<String>>): Completable = Observable.fromIterable(resFolders.keys)
            .map { platform.getStringsFilesExceptDefault(resFolder = it) }
            .flatMapIterable { it }
            .doOnNext { fileOperation.delete(it) }
            .ignoreElements()
}