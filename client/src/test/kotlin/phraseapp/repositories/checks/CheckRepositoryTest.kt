package phraseapp.repositories.checks

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import phraseapp.internal.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.network.LocaleContent
import phraseapp.network.PhraseAppNetworkDataSource
import java.io.File

class CheckRepositoryTest {
    @Test
    fun shouldGetFilePrintedInPhraseAppOutputsWhenThereAreErrorsInChecks() {
        val phraseAppNetworkDataSource: PhraseAppNetworkDataSource = mock {
            given { it.downloadAllLocales(any(), any(), any(), any()) }.willReturn(Single.just(mapOf(
                    "en" to LocaleContent(File("src/test/resources/android-errors/values/strings.xml").readText(), true),
                    "fr-FR" to LocaleContent(File("src/test/resources/android-errors/values-fr-rFR/strings.xml").readText(), false),
                    "es-ES" to LocaleContent(File("src/test/resources/android-errors/values-es-rES/strings.xml").readText(), false)
            )))
        }
        val printer = mock<FileOperation>()
        val repository = CheckRepositoryImpl("build", printer, "", phraseAppNetworkDataSource, Platform.ANDROID.toNewPlatform())
        val errors = repository.check()
                .test()
                .assertError(ChecksException::class.java)
                .errors()
                .first() as ChecksException

        assertEquals(4, errors.errors.size)
        verify(printer).print("build${File.separator}errors.txt", """
fr-FR :: PLURALS :: numberOfSongsAvailable
fr-FR :: PLACEHOLDER :: hello
fr-FR :: PLACEHOLDER :: numberOfSongsAvailable
es-ES :: PLURALS :: numberOfSongsAvailable
es-ES :: PLACEHOLDER :: hello
        """.trimIndent())
    }

    @Test
    fun shouldNotGetErrorsWhenThereIsNoErrorInStringsFiles() {
        val phraseAppNetworkDataSource: PhraseAppNetworkDataSource = mock {
            given { it.downloadAllLocales(any(), any(), any(), any()) }.willReturn(Single.just(mapOf(
                    "en" to LocaleContent(File("src/test/resources/android-local/values/strings.xml").readText(), true),
                    "fr-FR" to LocaleContent(File("src/test/resources/android-local/values-fr/strings.xml").readText(), false),
                    "es-ES" to LocaleContent(File("src/test/resources/android-local/values-es-rES/strings.xml").readText(), false)
            )))
        }
        val printer = mock<FileOperation>()
        val repository = CheckRepositoryImpl("build", printer, "", phraseAppNetworkDataSource, Platform.ANDROID.toNewPlatform())
        repository.check()
                .test()
                .assertNoErrors()

        verify(printer, times(0)).print(any(), any())
    }

    @Test
    fun shouldNotGetErrorWhenThereAreMissingTranslations() {
        val phraseAppNetworkDataSource: PhraseAppNetworkDataSource = mock {
            given { it.downloadAllLocales(any(), any(), any(), any()) }.willReturn(Single.just(mapOf(
                    "en" to LocaleContent(File("src/test/resources/android/values/strings.xml").readText(), true),
                    "fr-FR" to LocaleContent(File("src/test/resources/android/values-fr-rFR/strings.xml").readText(), false)
            )))
        }
        val printer = mock<FileOperation>()
        val repository = CheckRepositoryImpl("build", printer, "", phraseAppNetworkDataSource, Platform.ANDROID.toNewPlatform())
        repository.check()
                .test()
                .assertNoErrors()

        verify(printer, times(0)).print(any(), any())
    }
}