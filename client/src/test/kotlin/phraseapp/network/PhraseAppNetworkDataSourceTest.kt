package phraseapp.network

import org.junit.Assert.*
import org.junit.Test
import phraseapp.network.mock.MockPhraseAppService
import java.io.File

class PhraseAppNetworkDataSourceTest {
    private val service = MockPhraseAppService()

    @Test
    fun shouldDownloadAllXmlContentsExceptedDefaultLocale() {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales()
                .test()
                .assertNoErrors()
                .values()
                .first()
        assertEquals(2, xmlContents.size)
        assertTrue(xmlContents.containsKey("es-ES"))
        assertFalse(xmlContents["es-ES"]!!.isDefault)
        assertEquals(File("src/test/resources/android-remote/values-es-rES/strings.xml").readText(), xmlContents["es-ES"]?.content)
        assertTrue(xmlContents.containsKey("fr-FR"))
        assertFalse(xmlContents["fr-FR"]!!.isDefault)
        assertEquals(File("src/test/resources/android-remote/values-fr-rFR/strings.xml").readText(), xmlContents["fr-FR"]?.content)
    }

    @Test
    fun shouldDownloadAllXmlContentsIncludingDefaultLocale() {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales(overrideDefaultFile = true)
                .test()
                .assertNoErrors()
                .values()
                .first()
        assertEquals(3, xmlContents.size)
        assertTrue(xmlContents.containsKey("en"))
        assertTrue(xmlContents["en"]!!.isDefault)
        assertEquals(File("src/test/resources/android-remote/values/strings.xml").readText(), xmlContents["en"]?.content)
        assertTrue(xmlContents.containsKey("es-ES"))
        assertFalse(xmlContents["es-ES"]!!.isDefault)
        assertEquals(File("src/test/resources/android-remote/values-es-rES/strings.xml").readText(), xmlContents["es-ES"]?.content)
        assertTrue(xmlContents.containsKey("fr-FR"))
        assertFalse(xmlContents["fr-FR"]!!.isDefault)
        assertEquals(File("src/test/resources/android-remote/values-fr-rFR/strings.xml").readText(), xmlContents["fr-FR"]?.content)
    }

    @Test
    fun shouldSkipLocaleWhenLocaleDoNotMatchRegexApplyOnLocaleName() {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales(overrideDefaultFile = true, localeNameRegex = "")
                .test()
                .assertNoErrors()
                .values()
                .first()
        assertEquals(2, xmlContents.size)
        assertTrue(xmlContents.containsKey("en"))
        assertTrue(xmlContents.containsKey("fr-FR"))
    }

    @Test
    fun shouldRedirectLocaleToNewLocaleNameWhenItIsPresentInExceptionList() {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val xmlContents = networkDataSource.downloadAllLocales(exceptions = mapOf("es-ES" to "ca-ES"))
                .test()
                .assertNoErrors()
                .values()
                .first()
        assertEquals(2, xmlContents.size)
        assertFalse(xmlContents.containsKey("es-ES"))
        assertTrue(xmlContents.containsKey("ca-ES"))
    }

    @Test
    fun shouldUploadStringContent() {
        val networkDataSource = PhraseAppNetworkDataSourceImpl("", "", "", service)
        val results = networkDataSource.upload("", "")
                .test()
                .assertNoErrors()
                .values()
        assertEquals(0, results.size)
    }
}