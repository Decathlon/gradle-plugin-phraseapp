package phraseapp.repositories.operations

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation

class Cleaner(val platform: Platform, val fileOperation: FileOperation) {
    suspend fun clean(resFolders: Map<String, List<String>>) = coroutineScope {
        return@coroutineScope resFolders.keys
            .map { async { platform.getStringsFilesExceptDefault(resFolder = it) } }
            .awaitAll()
            .map { async { it.forEach { fileOperation.delete(it) } } }
    }
}