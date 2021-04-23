package phraseapp.repositories.operations

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import phraseapp.internal.platforms.Android
import phraseapp.internal.printers.FileOperation
import phraseapp.network.LocaleContent
import phraseapp.network.PhraseAppNetworkDataSource
import java.io.File

class DownloaderTest {
    @Test
    fun shouldDownloadAllResourcesWhenWeAskItForMultiModules() {
        val network: PhraseAppNetworkDataSource = mock {
            given { it.downloadAllLocales() }.willReturn(Single.just(mapOf(
                    "fr-FR" to LocaleContent(File("src/test/resources/android-remote/values-fr-rFR/strings.xml").readText(), false),
                    "es-ES" to LocaleContent(File("src/test/resources/android-remote/values-es-rES/strings.xml").readText(), false)
            )))
        }
        val fileOperation: FileOperation = mock()
        val resFolders = mapOf(
                "src/test/resources/android" to arrayListOf("strings.xml"),
                "src/test/resources/android-local" to arrayListOf("strings.xml")
        )
        val results = Downloader(Android, "build", fileOperation, network)
                .download(resFolders)
                .test()
                .assertNoErrors()
                .values()
        Assert.assertEquals(0, results.size)
        verify(fileOperation, times(5)).print(any(), any())
        verify(fileOperation).print(eq("build/languages.json"), eq("""{"FR":["fr"],"ES":["es"]}"""))
        verify(fileOperation).print(eq("src/test/resources/android/values-fr-rFR/strings.xml"), any())
        verify(fileOperation).print(eq("src/test/resources/android/values-es-rES/strings.xml"), any())
        verify(fileOperation).print(eq("src/test/resources/android-local/values-fr-rFR/strings.xml"), any())
        verify(fileOperation).print(eq("src/test/resources/android-local/values-es-rES/strings.xml"), any())
    }
}