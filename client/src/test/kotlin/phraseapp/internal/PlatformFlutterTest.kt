package phraseapp.internal

import org.junit.Assert.assertEquals
import org.junit.Test
import phraseapp.internal.platforms.Flutter
import phraseapp.repositories.operations.LanguageType

class PlatformFlutterTest {
    @Test
    fun testGetPathResFolderByDefaultLocale() {
        assertEquals("values", Flutter.getResPath(LanguageType("", isDefault = true)))
    }

    @Test
    fun testGetResourceFiles() {
        val files = Flutter.getStringsFilesExceptDefault("src/test/resources/flutter")
        assertEquals(1, files.size)
        assertEquals(
                "${System.getProperty("user.dir")}/src/test/resources/flutter/values/strings_fr.arb",
                files[0].absolutePath
        )
    }
}