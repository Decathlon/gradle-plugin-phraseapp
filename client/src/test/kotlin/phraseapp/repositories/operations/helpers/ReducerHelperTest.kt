package phraseapp.repositories.operations.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import phraseapp.extensions.ResourceTranslation
import phraseapp.internal.platforms.Android
import phraseapp.network.LocaleContent

class ReducerHelperTest {
    private val EMPTY = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
</resources>
"""
    private val EXAMPLE_1 = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="hello">Hello!</string>
    <plurals name="numberOfSongsAvailable">
        <item quantity="one">%d song found.</item>
        <item quantity="other">%d songs found.</item>
    </plurals>
</resources>
"""

    @Test
    fun testWhenThereIsNoStringsInRemote() {
        val helper = ReducerHelper(Android)
        val strings = helper.reduceKeysForAllStringsFilesAndForAllLocales(
                mapOf(createStringsFile("src/test/resources/android")),
                mapOf(createLocaleContent("en", EMPTY))
        )
        assertEquals(1, strings.keys.size)
        assertTrue(strings.containsKey("src/test/resources/android"))
        val translations = strings.getValue("src/test/resources/android")
        assertEquals(1, translations.size)
        assertEquals(1, translations.values.size)
        assertEquals(0, translations.values.first().strings.size)
    }

    @Test
    fun testWhenThereAreMoreStringsInRemoteThanLocal() {
        val helper = ReducerHelper(Android)
        val strings = helper.reduceKeysForAllStringsFilesAndForAllLocales(
                mapOf(createStringsFile("src/test/resources/android")),
                mapOf(createLocaleContent("fr-FR", EXAMPLE_1))
        )
        assertEquals(1, strings.keys.size)
        assertTrue(strings.containsKey("src/test/resources/android"))
        val translations = strings.getValue("src/test/resources/android")
        assertEquals(1, translations.size)
        assertEquals(1, translations.values.size)
        assertEquals(1, translations.values.first().strings.size)
    }

    @Test
    fun testWhenThereAreMultipleResFolders() {
        val helper = ReducerHelper(Android)
        val strings = helper.reduceKeysForAllStringsFilesAndForAllLocales(
                mapOf(createStringsFile("src/test/resources/android"), createStringsFile("src/test/resources/android-local")),
                mapOf(createLocaleContent("fr-FR", EXAMPLE_1))
        )
        assertEquals(2, strings.keys.size)
        assertTrue(strings.keys.contains("src/test/resources/android"))
        val android = strings.getValue("src/test/resources/android")
        assertEquals(1, android.values.size)
        assertEquals(1, android.values.size)
        assertEquals(1, android.values.first().strings.size)

        assertTrue(strings.keys.contains("src/test/resources/android-local"))
        val androidLocal = strings.getValue("src/test/resources/android-local")
        assertEquals(1, androidLocal.values.size)
        assertEquals(1, androidLocal.values.size)
        assertEquals(1, androidLocal.values.first().strings.size)
    }

    private fun createStringsFile(resFolder: String): Pair<String, ResourceTranslation> {
        val stringsFile = LocalHelper(Android).getStringsFileByResFolder(mapOf(resFolder to arrayListOf("strings.xml")))
        return resFolder to (stringsFile[resFolder] ?: error("$resFolder don't have any strings.xml file"))
    }

    private fun createLocaleContent(locale: String, content: String): Pair<String, LocaleContent> {
        return locale to LocaleContent(content.trimIndent(), locale == "en")
    }
}