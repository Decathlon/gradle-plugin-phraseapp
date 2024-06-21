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
    fun shouldDownloadAllXmlContentsIncludingDefaultLocale() = runBlocking {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales()
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
        val xmlContents = networkDataSource.downloadAllLocales(localeNameRegex = "")
        assertEquals(2, xmlContents.size)
        assertTrue(xmlContents.containsKey("en"))
        assertTrue(xmlContents.containsKey("fr-FR"))
    }

    @Test
    fun shouldSkipLocaleWhenLocaleCodeNotAllowed() = runBlocking {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales(allowedLocaleCodes = listOf("fr-FR"))
        assertEquals(1, xmlContents.size)
        assertTrue(xmlContents.containsKey("fr-FR"))
    }

    @Test
    fun shouldRedirectLocaleToNewLocaleNameWhenItIsPresentInExceptionList() = runBlocking {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales(exceptions = mapOf("es-ES" to "ca-ES"))
        assertEquals(3, xmlContents.size)
        assertFalse(xmlContents.containsKey("es-ES"))
        assertTrue(xmlContents.containsKey("ca-ES"))
        assertTrue(xmlContents.containsKey("en"))
        assertTrue(xmlContents.containsKey("fr-FR"))
    }

    @Test
    fun shouldUploadStringContent() = runBlocking {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val results = networkDataSource.upload("", "")
        assertNotNull(results)
    }
}