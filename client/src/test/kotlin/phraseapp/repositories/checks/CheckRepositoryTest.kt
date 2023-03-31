package phraseapp.repositories.checks

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import phraseapp.internal.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.network.LocaleContent
import phraseapp.network.PhraseAppNetworkDataSource
import java.io.File

class CheckRepositoryTest {
    @Test
    fun shouldGetFilePrintedInPhraseAppOutputsWhenThereAreErrorsInChecks() = runBlocking {
        val phraseAppNetworkDataSource: PhraseAppNetworkDataSource = mock {
            `when`(it.downloadAllLocales(any(), any(), any(), any(), any())).thenReturn(mapOf(
                "en" to LocaleContent(File("src/test/resources/android-errors/values/strings.xml").readText(), true),
                "fr-FR" to LocaleContent(File("src/test/resources/android-errors/values-fr-rFR/strings.xml").readText(), false),
                "es-ES" to LocaleContent(File("src/test/resources/android-errors/values-es-rES/strings.xml").readText(), false)
            ))
        }
        val printer = mock<FileOperation>()
        val repository = CheckRepositoryImpl("build", printer, "", phraseAppNetworkDataSource, Platform.ANDROID.toNewPlatform())

        try {
            repository.check()
        } catch (error: ChecksException) {
            assertEquals(4, error.errors.size)
            verify(printer).print("build${File.separator}errors.txt", """
fr-FR :: PLURALS :: numberOfSongsAvailable
fr-FR :: PLACEHOLDER :: hello
fr-FR :: PLACEHOLDER :: numberOfSongsAvailable
es-ES :: PLURALS :: numberOfSongsAvailable
es-ES :: PLACEHOLDER :: hello
        """.trimIndent())
        }
    }

    @Test
    fun shouldNotGetErrorsWhenThereIsNoErrorInStringsFiles() = runBlocking {
        val phraseAppNetworkDataSource: PhraseAppNetworkDataSource = mock {
            `when`(it.downloadAllLocales(any(), any(), any(), any(), any())).thenReturn(mapOf(
                "en" to LocaleContent(File("src/test/resources/android-local/values/strings.xml").readText(), true),
                "fr-FR" to LocaleContent(File("src/test/resources/android-local/values-fr/strings.xml").readText(), false),
                "es-ES" to LocaleContent(File("src/test/resources/android-local/values-es-rES/strings.xml").readText(), false)
            ))
        }
        val printer = mock<FileOperation>()
        val repository = CheckRepositoryImpl("build", printer, "", phraseAppNetworkDataSource, Platform.ANDROID.toNewPlatform())
        repository.check()
        verify(printer, times(0)).print(any(), any())
    }

    @Test
    fun shouldNotGetErrorWhenThereAreMissingTranslations() = runBlocking {
        val phraseAppNetworkDataSource: PhraseAppNetworkDataSource = mock {
            `when`(it.downloadAllLocales(any(), any(), any(), any(), any())).thenReturn(mapOf(
                "en" to LocaleContent(File("src/test/resources/android/values/strings.xml").readText(), true),
                "fr-FR" to LocaleContent(File("src/test/resources/android/values-fr-rFR/strings.xml").readText(), false)
            ))
        }
        val printer = mock<FileOperation>()
        val repository = CheckRepositoryImpl("build", printer, "", phraseAppNetworkDataSource, Platform.ANDROID.toNewPlatform())
        repository.check()
        verify(printer, times(0)).print(any(), any())
    }
}