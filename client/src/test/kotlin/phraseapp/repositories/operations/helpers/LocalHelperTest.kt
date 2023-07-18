package phraseapp.repositories.operations.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import phraseapp.internal.exception.DuplicateKeyException
import phraseapp.internal.platforms.Android

class LocalHelperTest {
    @Test
    fun testWhenThereIsOnlyOneStringsFileInResFolder() {
        val helper = LocalHelper(Android)
        val stringsFiles =
            helper.getStringsFileByResFolder(mapOf("src/test/resources/android" to arrayListOf("strings.xml")))
        assertEquals(1, stringsFiles.size)
        assertEquals("src/test/resources/android", stringsFiles.keys.first())
        assertEquals(1, stringsFiles.values.size)
        assertEquals(1, stringsFiles.values.toList()[0].plurals.size)
    }

    @Test
    fun testWhenThereAreTwoStringsFilesInResFolder() {
        val helper = LocalHelper(Android)
        val stringsFiles = helper.getStringsFileByResFolder(
            mapOf(
                "src/test/resources/android-multi-strings" to arrayListOf(
                    "strings.xml",
                    "strings-2.xml"
                )
            )
        )
        assertEquals(1, stringsFiles.size)
        assertEquals("src/test/resources/android-multi-strings", stringsFiles.keys.first())
        assertEquals(1, stringsFiles.values.size)
        assertEquals(2, stringsFiles.values.toList()[0].strings.size)
    }

    @Test
    fun testWhenThereAreMultipleStringsFilesInMultipleResFolders() {
        val helper = LocalHelper(Android)
        val stringsFiles = helper.getStringsFileByResFolder(
            mapOf(
                "src/test/resources/android" to arrayListOf("strings.xml"),
                "src/test/resources/android-local" to arrayListOf("strings.xml"),
                "src/test/resources/android-multi-strings" to arrayListOf("strings.xml", "strings-2.xml")
            )
        )
        assertTrue(stringsFiles.containsKey("src/test/resources/android"))
        assertEquals(arrayListOf("numberOfSongsAvailable"), stringsFiles.getValue("src/test/resources/android").keys)
        assertTrue(stringsFiles.containsKey("src/test/resources/android-local"))
        assertEquals(
            arrayListOf("hello", "world", "worlds"),
            stringsFiles.getValue("src/test/resources/android-local").keys
        )
        assertTrue(stringsFiles.containsKey("src/test/resources/android-multi-strings"))
        assertEquals(
            arrayListOf("hello", "world"),
            stringsFiles.getValue("src/test/resources/android-multi-strings").keys
        )
    }

    @Test(expected = DuplicateKeyException::class)
    fun testWhenThereAreTwoStringsFilesInResFolderAndSameKeys() {
        val helper = LocalHelper(Android)
        helper.getStringsFileByResFolder(
            resFolders = mapOf(
                "src/test/resources/android-multi-strings" to arrayListOf(
                    "strings.xml",
                    "strings-3.xml"
                )
            ), checkDuplicateKeys = true
        )
    }

    @Test
    fun testWhenThereIsNoStringsFileSpecified() {
        val helper = LocalHelper(Android)
        val stringsFiles = helper.getStringsFileByResFolder(mapOf("src/test/resources/android" to emptyList()))
        assertEquals(1, stringsFiles.size)
        assertEquals(0, stringsFiles.values.toList()[0].strings.size)
        assertEquals(0, stringsFiles.values.toList()[0].plurals.size)
    }

    @Test
    fun shouldGetKeysFromDefaultStringsFile() {
        val helper = LocalHelper(Android)
        val stringsFiles =
            helper.getStringsFileByResFolder(mapOf("src/test/resources/android-local" to arrayListOf("strings.xml")))
        assertTrue(stringsFiles.containsKey("src/test/resources/android-local"))
        val resource = stringsFiles.getValue("src/test/resources/android-local")
        assertEquals(arrayListOf("hello", "world", "worlds"), resource.keys)
    }

    @Test(expected = NoSuchFileException::class)
    fun shouldThrowAnExceptionWhenThereIsNoDefaultStringFile() {
        val helper = LocalHelper(Android)
        helper.getStringsFileByResFolder(mapOf("src/test/resources/android-local" to arrayListOf("unknown.xml")))
    }
}