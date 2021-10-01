package phraseapp.network

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import phraseapp.network.mock.MockPhraseAppService
import java.io.File

class PhraseAppNetworkDataSourceTest {
    private val service = MockPhraseAppService()

    @Test
    fun shouldDownloadAllXmlContentsExceptedDefaultLocale() = runBlocking {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales()
        assertEquals(2, xmlContents.size)
        assertTrue(xmlContents.containsKey("es-ES"))
        assertFalse(xmlContents["es-ES"]!!.isDefault)
        assertEquals(
            File("src/test/resources/android-remote/values-es-rES/strings.xml").readText(),
            xmlContents["es-ES"]?.content
        )
        assertTrue(xmlContents.containsKey("fr-FR"))
        assertFalse(xmlContents["fr-FR"]!!.isDefault)
        assertEquals(
            File("src/test/resources/android-remote/values-fr-rFR/strings.xml").readText(),
            xmlContents["fr-FR"]?.content
        )
    }

    @Test
    fun shouldDownloadAllXmlContentsIncludingDefaultLocale() = runBlocking {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales(overrideDefaultFile = true)
        assertEquals(3, xmlContents.size)
        assertTrue(xmlContents.containsKey("en"))
        assertTrue(xmlContents["en"]!!.isDefault)
        assertEquals(
            File("src/test/resources/android-remote/values/strings.xml").readText(),
            xmlContents["en"]?.content
        )
        assertTrue(xmlContents.containsKey("es-ES"))
        assertFalse(xmlContents["es-ES"]!!.isDefault)
        assertEquals(
            File("src/test/resources/android-remote/values-es-rES/strings.xml").readText(),
            xmlContents["es-ES"]?.content
        )
        assertTrue(xmlContents.containsKey("fr-FR"))
        assertFalse(xmlContents["fr-FR"]!!.isDefault)
        assertEquals(
            File("src/test/resources/android-remote/values-fr-rFR/strings.xml").readText(),
            xmlContents["fr-FR"]?.content
        )
    }

    @Test
    fun shouldSkipLocaleWhenLocaleDoNotMatchRegexApplyOnLocaleName() = runBlocking {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales(overrideDefaultFile = true, localeNameRegex = "")
        assertEquals(2, xmlContents.size)
        assertTrue(xmlContents.containsKey("en"))
        assertTrue(xmlContents.containsKey("fr-FR"))
    }

    @Test
    fun shouldRedirectLocaleToNewLocaleNameWhenItIsPresentInExceptionList() = runBlocking {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales(exceptions = mapOf("es-ES" to "ca-ES"))
        assertEquals(2, xmlContents.size)
        assertFalse(xmlContents.containsKey("es-ES"))
        assertTrue(xmlContents.containsKey("ca-ES"))
    }

    @Test
    fun shouldUploadStringContent() = runBlocking {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val results = networkDataSource.upload("", "")
        assertNotNull(results)
    }
}