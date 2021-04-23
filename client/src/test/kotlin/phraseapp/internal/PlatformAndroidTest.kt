package phraseapp.internal

import org.junit.Assert.assertEquals
import org.junit.Test
import phraseapp.internal.platforms.Android
import phraseapp.repositories.operations.DefaultType
import phraseapp.repositories.operations.LanguageType
import phraseapp.repositories.operations.LocaleType

class PlatformAndroidTest {
    @Test
    fun testGetPathResFolderByDefaultLocale() {
        assertEquals("values", Android.getResPath(DefaultType))
    }

    @Test
    fun testGetPathResFolderByLocale() {
        assertEquals("values-fr-rFR", Android.getResPath(LocaleType("fr", "FR")))
    }

    @Test
    fun testGetPathResFolderByLanguage() {
        assertEquals("values-fr", Android.getResPath(LanguageType("fr")))
    }

    @Test
    fun testGetResourceFiles() {
        val files = Android.getStringsFilesExceptDefault("src/test/resources/android")
        assertEquals(1, files.size)
        assertEquals(
                "${System.getProperty("user.dir")}/src/test/resources/android/values-fr-rFR/strings.xml",
                files[0].absolutePath
        )
    }

    @Test
    fun testGetResourceFilesWithUnknownResFolder() {
        val files = Android.getStringsFilesExceptDefault(".")
        assertEquals(0, files.size)
    }

    @Test
    fun testGetResourceFilesWithInvalidResFolder() {
        val files = Android.getStringsFilesExceptDefault("zadhuzah")
        assertEquals(0, files.size)
    }
}