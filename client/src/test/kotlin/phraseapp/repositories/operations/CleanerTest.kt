package phraseapp.repositories.operations

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import phraseapp.internal.platforms.Android
import phraseapp.internal.platforms.iOS
import phraseapp.internal.printers.FileOperation
import java.io.File

class CleanerTest {
    @Test
    fun shouldCleanResourceFilesWhenThereAreStringFilesTranslated() = runBlocking {
        val fileOperation: FileOperation = mock()
        val rootDir = System.getProperty("user.dir")
        val resFolder = "src/test/resources/android"
        Cleaner(Android, fileOperation).clean(mapOf(resFolder to arrayListOf("strings.xml")))
        verify(fileOperation, times(1)).delete(any())
        verify(fileOperation).delete(File("$rootDir${File.separator}$resFolder${File.separator}values-fr-rFR${File.separator}strings.xml"))
    }

    @Test
    fun shouldCleanResourceFilesWhenThereAreMultipleResFolders() = runBlocking {
        val fileOperation: FileOperation = mock()
        val rootDir = System.getProperty("user.dir")
        val resFolderModule1 = "src/test/resources/android" to arrayListOf("strings.xml")
        val resFolderModule2 = "src/test/resources/android-remote" to arrayListOf("strings.xml")
        Cleaner(Android, fileOperation).clean(mapOf(resFolderModule1, resFolderModule2))
        verify(fileOperation, times(4)).delete(any())
        verify(fileOperation).delete(File("$rootDir${File.separator}src${File.separator}test${File.separator}resources${File.separator}android${File.separator}values-fr-rFR${File.separator}strings.xml"))
        verify(fileOperation).delete(File("$rootDir${File.separator}src${File.separator}test${File.separator}resources${File.separator}android-remote${File.separator}values-es-rES${File.separator}strings.xml"))
        verify(fileOperation).delete(File("$rootDir${File.separator}src${File.separator}test${File.separator}resources${File.separator}android-remote${File.separator}values-fr-rFR${File.separator}strings.xml"))
    }

    @Test
    fun shouldCleanResourcesFilesWhenThereArePotentiallySeveralResFilesForOneCountry() = runBlocking {
        val fileOperation: FileOperation = mock()
        val rootDir = System.getProperty("user.dir")
        val resFolder = "src/test/resources/ios" to arrayListOf("Localizable.strings")
        Cleaner(iOS, fileOperation).clean(mapOf(resFolder))
        verify(fileOperation, times(1)).delete(any())
        verify(fileOperation).delete(File("$rootDir${File.separator}src${File.separator}test${File.separator}resources${File.separator}ios${File.separator}fr-FR.lproj${File.separator}Localizable.strings"))
    }

    @Test
    fun shouldNotCleanResourcesFilesWhenThereIsNoResourcesToClean() = runBlocking {
        val fileOperation: FileOperation = mock()
        val resFolder = "src/test/resources/unknown" to arrayListOf("strings.xml")
        Cleaner(Android, fileOperation).clean(mapOf(resFolder))
        verify(fileOperation, times(0)).delete(any())
    }
}