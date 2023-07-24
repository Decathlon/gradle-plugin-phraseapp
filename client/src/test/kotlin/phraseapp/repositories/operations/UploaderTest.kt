package phraseapp.repositories.operations

import assertk.fail
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import phraseapp.internal.platforms.Android
import phraseapp.network.PhraseAppNetworkDataSource
import java.io.File

class UploaderTest {
    @Test
    fun shouldUploadXmlFileWithMultipleStringResources() = runBlocking {
        val network: PhraseAppNetworkDataSource = mock()
        whenever(network.upload(eq("localeId"), any())).thenReturn(Unit)
        val resFolders = mapOf(
                "src/test/resources/android" to arrayListOf("strings.xml"),
                "src/test/resources/android-local" to arrayListOf("strings.xml")
        )

        Uploader(Android, "build", mock(), network)
                .upload("localeId", resFolders)

        verify(network, times(1)).upload("localeId", "build${File.separator}strings.xml")
    }

    @Test
    fun shouldNotUploadXmlFileWhenStringXmlFileNotExist() = runBlocking {
        try {
            Uploader(Android, "build", mock(), mock())
                .upload("localeId", mapOf("src/test/resources/unknown" to arrayListOf("strings.xml")))
            fail("Should throw NoSuchFileException")
        } catch(error: NoSuchFileException) {
            // Everything is fine
        } catch (error: Throwable) {
            fail("Should throw NoSuchFileException")
        }
    }
}