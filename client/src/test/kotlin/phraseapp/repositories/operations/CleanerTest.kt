package phraseapp.repositories.operations

import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import phraseapp.internal.platforms.Android
import phraseapp.internal.platforms.iOS
import phraseapp.internal.printers.FileOperation
import java.io.File

class CleanerTest {
    @Test
    fun shouldCleanResourceFilesWhenThereAreStringFilesTranslated() {
        val fileOperation: FileOperation = mock()
        val rootDir = System.getProperty("user.dir")
        val resFolder = "src/test/resources/android"
        Cleaner(Android, fileOperation)
                .clean(mapOf(resFolder to arrayListOf("strings.xml")))
                .test()
                .assertNoErrors()
        verify(fileOperation, times(1)).delete(any())
        verify(fileOperation).delete(eq(File("$rootDir${File.separator}$resFolder${File.separator}values-fr-rFR${File.separator}strings.xml")))
    }

    @Test
    fun shouldCleanResourceFilesWhenThereAreMultipleResFolders() {
        val fileOperation: FileOperation = mock()
        val rootDir = System.getProperty("user.dir")
        val resFolderModule1 = "src/test/resources/android" to arrayListOf("strings.xml")
        val resFolderModule2 = "src/test/resources/android-remote" to arrayListOf("strings.xml")
        Cleaner(Android, fileOperation)
                .clean(mapOf(resFolderModule1, resFolderModule2))
                .test()
                .assertNoErrors()
        verify(fileOperation, times(3)).delete(any())
        verify(fileOperation).delete(eq(File("$rootDir${File.separator}src${File.separator}test${File.separator}resources${File.separator}android${File.separator}values-fr-rFR${File.separator}strings.xml")))
        verify(fileOperation).delete(eq(File("$rootDir${File.separator}src${File.separator}test${File.separator}resources${File.separator}android-remote${File.separator}values-es-rES${File.separator}strings.xml")))
        verify(fileOperation).delete(eq(File("$rootDir${File.separator}src${File.separator}test${File.separator}resources${File.separator}android-remote${File.separator}values-fr-rFR${File.separator}strings.xml")))
    }

    @Test
    fun shouldCleanResourcesFilesWhenThereArePotentiallySeveralResFilesForOneCountry() {
        val fileOperation: FileOperation = mock()
        val rootDir = System.getProperty("user.dir")
        val resFolder = "src/test/resources/ios" to arrayListOf("Localizable.strings")
        Cleaner(iOS, fileOperation)
                .clean(mapOf(resFolder))
                .test()
                .assertNoErrors()
        verify(fileOperation, times(1)).delete(any())
        verify(fileOperation).delete(eq(File("$rootDir${File.separator}src${File.separator}test${File.separator}resources${File.separator}ios${File.separator}fr-FR.lproj${File.separator}Localizable.strings")))
    }

    @Test
    fun shouldNotCleanResourcesFilesWhenThereIsNoResourcesToClean() {
        val fileOperation: FileOperation = mock()
        val resFolder = "src/test/resources/unknown" to arrayListOf("strings.xml")
        Cleaner(Android, fileOperation)
                .clean(mapOf(resFolder))
                .test()
                .assertNoErrors()
        verify(fileOperation, times(0)).delete(any())
    }
}