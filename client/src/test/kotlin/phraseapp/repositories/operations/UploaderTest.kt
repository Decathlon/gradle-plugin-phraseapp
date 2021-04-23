package phraseapp.repositories.operations

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import org.junit.Assert.assertEquals
import org.junit.Test
import phraseapp.internal.platforms.Android
import phraseapp.network.PhraseAppNetworkDataSource
import java.io.File

class UploaderTest {
    @Test
    fun shouldUploadXmlFileWithMultipleStringResources() {
        val network: PhraseAppNetworkDataSource = mock {
            given { it.upload(eq("localeId"), any()) }.willReturn(Completable.complete())
        }
        val resFolders = mapOf(
                "src/test/resources/android" to arrayListOf("strings.xml"),
                "src/test/resources/android-local" to arrayListOf("strings.xml")
        )

        val results = Uploader(Android, "build", mock(), network)
                .upload("localeId", resFolders)
                .test()
                .assertNoErrors()
                .values()
        assertEquals(0, results.size)
        verify(network, times(1)).upload("localeId", "build${File.separator}strings.xml")
    }

    @Test
    fun shouldNotUploadXmlFileWhenStringXmlFileNotExist() {
        Uploader(Android, "build", mock(), mock())
                .upload("localeId", mapOf("src/test/resources/unknown" to arrayListOf("strings.xml")))
                .test()
                .assertError(NoSuchFileException::class.java)
    }
}