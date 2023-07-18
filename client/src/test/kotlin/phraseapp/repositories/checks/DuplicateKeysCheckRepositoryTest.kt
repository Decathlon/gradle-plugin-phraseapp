package phraseapp.repositories.checks

import kotlinx.coroutines.runBlocking
import org.junit.Test
import phraseapp.internal.exception.DuplicateKeyException
import phraseapp.internal.platforms.Android

class DuplicateKeysCheckRepositoryTest {
    @Test(expected = DuplicateKeyException::class)
    fun testWhenThereAreTwoStringsFilesInResFolderAndSameKeys() = runBlocking {
        val repository = DuplicateKeysCheckRepository(Android)
        repository.check(
            resFolders = mapOf(
                "src/test/resources/android-multi-strings" to arrayListOf(
                    "strings.xml",
                    "strings-3.xml"
                )
            )
        )
    }
}
