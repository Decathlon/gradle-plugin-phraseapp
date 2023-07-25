package phraseapp.repositories.checks

import kotlinx.coroutines.coroutineScope
import phraseapp.internal.platforms.Platform
import phraseapp.repositories.operations.helpers.LocalHelper

class DuplicateKeysRepository(
    platform: Platform
) {
    private val localHelper = LocalHelper(platform)

    suspend fun check(resFolders: Map<String, List<String>>) = coroutineScope {
        localHelper.mergeStringsFilesByResFolder(resFolders = resFolders, checkDuplicateKeys = true)
    }
}