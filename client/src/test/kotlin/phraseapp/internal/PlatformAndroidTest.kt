package phraseapp.internal

import org.junit.Assert.assertEquals
import org.junit.Test
import phraseapp.internal.platforms.Android
import phraseapp.repositories.operations.LanguageType
import phraseapp.repositories.operations.LocaleType

class PlatformAndroidTest {
    @Test
    fun testGetPathResFolderByDefaultLocale() {
        assertEquals("values", Android.getResPath(LanguageType("", isDefault = true)))
    }

    @Test
    fun testGetPathResFolderByLocale() {
        assertEquals("values-fr-rFR", Android.getResPath(LocaleType("fr", "FR", false)))
    }

    @Test
    fun testGetPathResFolderByLanguage() {
        assertEquals("values-fr", Android.getResPath(LanguageType("fr", false)))
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