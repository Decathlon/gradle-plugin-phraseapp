package phraseapp.internal

import org.junit.Assert.assertEquals
import org.junit.Test
import phraseapp.internal.platforms.iOS
import phraseapp.repositories.operations.LanguageType
import phraseapp.repositories.operations.LocaleType

class PlatformIOSTest {
    @Test
    fun testGetPathResFolderByDefaultLocale() {
        assertEquals("Base.lproj", iOS.getResPath(LanguageType("", isDefault = true)))
    }

    @Test
    fun testGetPathResFolderByLocale() {
        assertEquals("fr-FR.lproj", iOS.getResPath(LocaleType("fr", "FR", false)))
    }

    @Test
    fun testGetPathResFolderByLanguage() {
        assertEquals("fr.lproj", iOS.getResPath(LanguageType("fr",false)))
    }

    @Test
    fun testGetResourceFiles() {
        val files = iOS.getStringsFilesExceptDefault("src/test/resources/ios")
        assertEquals(1, files.size)
        assertEquals(
                "${System.getProperty("user.dir")}/src/test/resources/ios/fr-FR.lproj/Localizable.strings",
                files[0].absolutePath
        )
    }

    @Test
    fun testGetResourceFilesWithUnknownResFolder() {
        val files = iOS.getStringsFilesExceptDefault(".")
        assertEquals(0, files.size)
    }

    @Test
    fun testGetResourceFilesWithInvalidResFolder() {
        val files = iOS.getStringsFilesExceptDefault("zadhuzah")
        assertEquals(0, files.size)
    }
}