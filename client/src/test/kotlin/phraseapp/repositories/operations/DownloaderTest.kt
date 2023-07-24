package phraseapp.repositories.operations

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import phraseapp.internal.platforms.Android
import phraseapp.internal.printers.FileOperation
import phraseapp.network.LocaleContent
import phraseapp.network.PhraseAppNetworkDataSource
import java.io.File

class DownloaderTest {
    @Test
    fun shouldDownloadAllResourcesWhenWeAskItForMultiModules() = runBlocking {
        val network: PhraseAppNetworkDataSource = mock()
        whenever(network.downloadAllLocales()).thenReturn(
            mapOf(
                "fr-FR" to LocaleContent(File("src/test/resources/android-remote/values-fr-rFR/strings.xml").readText(), false),
                "es-ES" to LocaleContent(File("src/test/resources/android-remote/values-es-rES/strings.xml").readText(), false)
            )
        )
        val fileOperation: FileOperation = mock()
        val resFolders = mapOf(
                "src/test/resources/android" to arrayListOf("strings.xml"),
                "src/test/resources/android-local" to arrayListOf("strings.xml")
        )
        val results = Downloader(Android, "build", fileOperation, network).download(resFolders)
        Assert.assertEquals(2, results.size)
        verify(fileOperation, times(5)).print(any(), any())
        verify(fileOperation).print(eq("build/languages.json"), eq("""{"FR":["fr"],"ES":["es"]}"""))
        verify(fileOperation).print(eq("src/test/resources/android/values-fr-rFR/strings.xml"), any())
        verify(fileOperation).print(eq("src/test/resources/android/values-es-rES/strings.xml"), any())
        verify(fileOperation).print(eq("src/test/resources/android-local/values-fr-rFR/strings.xml"), any())
        verify(fileOperation).print(eq("src/test/resources/android-local/values-es-rES/strings.xml"), any())
    }
}