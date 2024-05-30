package phraseapp.repositories.operations.helpers

import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import phraseapp.extensions.parse
import phraseapp.internal.platforms.Android
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.xml.Resource
import phraseapp.repositories.operations.LanguageType
import phraseapp.repositories.operations.LocaleType
import phraseapp.repositories.operations.ResFolderType
import java.io.File

class PrinterHelperTest {
    private val EMPTY = """
<resources>

</resources>
"""
    private val EXAMPLE_1 = """
<resources>
	<string name="hello">Hello!</string>
	<plurals name="numberOfSongsAvailable">
		<item quantity="one">%d song found.</item>
		<item quantity="other">%d songs found.</item>
	</plurals>
</resources>
"""

    @Test
    fun testWhenPrintEmptyLocales() {
        val fileOperation: FileOperation = mock()
        val helper = PrinterHelper(Android, "build", fileOperation)
        helper.printLocales(emptyList())

        verify(fileOperation).print("build${File.separator}languages.json", "{}")
    }

    @Test
    fun testWhenPrintLocalesWithSameCountries() {
        val fileOperation: FileOperation = mock()
        val helper = PrinterHelper(Android, "build", fileOperation)
        helper.printLocales(arrayListOf(
                LocaleType("fr", "FR", false),
                LanguageType("en", false),
                LocaleType("fr", "BE", false),
                LocaleType("nl", "BE", false)
        ))

        verify(fileOperation).print("build${File.separator}languages.json", """
{"FR":["fr"],"BE":["fr","nl"]}
        """.trimIndent())
    }

    @Test
    fun testWhenThereIsNoStringsOrPlurals() {
        val fileOperation: FileOperation = mock()
        val helper = PrinterHelper(Android, "build", fileOperation)
        val mapOf: Map<ResFolderType, Resource> = mapOf(
                LanguageType(language = "", isDefault = true) to getResource(EMPTY)
        )
        helper.printResources(mapOf("src/test/resources/android" to mapOf), true)

        val expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>${EMPTY}".trimIndent()
        verify(fileOperation).print("src/test/resources/android/values/strings.xml", expected)
    }

    @Test
    fun testWhenPrintResourcesDefaultFileIsDuplicated() {
        val fileOperation: FileOperation = mock()
        val helper = PrinterHelper(Android, "build", fileOperation)
        val mapOf: Map<ResFolderType, Resource> = mapOf(
            LanguageType(language = "fr", isDefault = true) to getResource(EMPTY),
            LocaleType(language = "fr", country = "FR", isDefault = false) to getResource(EMPTY),
            LocaleType(language = "en", country = "GB", isDefault = false) to getResource(EMPTY),
            LocaleType(language = "en", country = "IE", isDefault = false) to getResource(EMPTY),
        )
        helper.printResources(mapOf("src/test/resources/android" to mapOf), false)

        verify(fileOperation).copy("src/test/resources/android/values/strings.xml",
            "src/test/resources/android/values-fr/strings.xml")
    }

    @Test
    fun testWhenPrintResourcesDefaultFileIsNotDuplicated() {
        val fileOperation: FileOperation = mock()
        val helper = PrinterHelper(Android, "build", fileOperation)
        val mapOf: Map<ResFolderType, Resource> = mapOf(
            LanguageType(language = "fr", isDefault = true) to getResource(EMPTY),
            LocaleType(language = "en", country = "GB", isDefault = false) to getResource(EMPTY),
            LocaleType(language = "en", country = "IE", isDefault = false) to getResource(EMPTY),
        )
        helper.printResources(mapOf("src/test/resources/android" to mapOf), false)

        verify(fileOperation, Mockito.times(0)).copy(any(), any())

    }

    @Test
    fun testWhenThereAreStringsAndPluralsToPrint() {
        val fileOperation: FileOperation = mock()
        val helper = PrinterHelper(Android, "build", fileOperation)
        val mapOf: Map<ResFolderType, Resource> = mapOf(
            LanguageType(language = "", isDefault = true) to getResource(EXAMPLE_1)
        )
        helper.printResources(mapOf("src/test/resources/android" to mapOf), true)

        val expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>${EXAMPLE_1}".trimIndent()
        verify(fileOperation).print("src/test/resources/android/values/strings.xml", expected)
    }

    @Test
    fun testWhenTranslatableAttrIsSetToFalse() {
        val fileOperation: FileOperation = mock()
        val helper = PrinterHelper(Android, "build", fileOperation)
        helper.printTempStringsFile(getResource("""
<resources>
	<string name="translatable">Should be send to Phrase!</string>
	<string name="notTranslatable" translatable="false">Shouldn't be send to Phrase!</string>
</resources>
        """.trimIndent()))

        val expected = """<?xml version="1.0" encoding="UTF-8"?>
<resources>
	<string name="translatable">Should be send to Phrase!</string>
</resources>
""".trimIndent()
        verify(fileOperation).print(helper.tempStringFilePath, expected)
    }

    private fun getResource(content: String): Resource {
        val resource = content.parse(Android.format)
        return Resource(resource.strings + resource.plurals)
    }
}