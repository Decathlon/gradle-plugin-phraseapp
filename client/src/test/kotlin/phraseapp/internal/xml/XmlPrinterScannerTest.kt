package phraseapp.internal.xml

import org.junit.Assert
import org.junit.Test

class XmlPrinterScannerTest {
    @Test
    fun shouldGenerateXmlContentWithStringAndPlurals() {
        val resource = Resource(arrayListOf(
                StringTranslation("test", "Test", "test"),
                PluralsTranslation("test", arrayListOf(
                        StringTranslation("one", "%d song found.", "test"),
                        StringTranslation("other", "%d songs found.", "test")
                ), "test")
        ))
        val expected = """
<?xml version="1.0" encoding="UTF-8"?>
<resources>
	<string name="test">Test</string>
	<plurals name="test">
		<item quantity="one">%d song found.</item>
		<item quantity="other">%d songs found.</item>
	</plurals>
</resources>
        """.trimIndent()
        Assert.assertEquals(expected, XmlPrinterScanner().start(resource))
    }

    @Test
    fun shouldGenerateXmlContentWithMultipleString() {
        val resource = Resource(arrayListOf(
                StringTranslation("test", "Test", "test"),
                StringTranslation("one", "%d song found.", "test"),
                StringTranslation("other", "%d songs found.", "test")
        ))
        val expected = """
<?xml version="1.0" encoding="UTF-8"?>
<resources>
	<string name="test">Test</string>
	<string name="one">%d song found.</string>
	<string name="other">%d songs found.</string>
</resources>
        """.trimIndent()
        Assert.assertEquals(expected, XmlPrinterScanner().start(resource))
    }

    @Test
    fun shouldGenerateXmlContentWithMultiplePlurals() {
        val resource = Resource(arrayListOf(
                PluralsTranslation("test", arrayListOf(
                        StringTranslation("one", "%d song found.", "test"),
                        StringTranslation("other", "%d songs found.", "test")
                ), "test"),
                PluralsTranslation("test", arrayListOf(
                        StringTranslation("one", "%d song found.", "test"),
                        StringTranslation("other", "%d songs found.", "test")
                ), "test")
        ))
        val expected = """
<?xml version="1.0" encoding="UTF-8"?>
<resources>
	<plurals name="test">
		<item quantity="one">%d song found.</item>
		<item quantity="other">%d songs found.</item>
	</plurals>
	<plurals name="test">
		<item quantity="one">%d song found.</item>
		<item quantity="other">%d songs found.</item>
	</plurals>
</resources>
        """.trimIndent()
        Assert.assertEquals(expected, XmlPrinterScanner().start(resource))
    }

    @Test
    fun shouldGenerateXmlContentWithEmptyString() {
        val resource = Resource(emptyList())
        val expected = """
<?xml version="1.0" encoding="UTF-8"?>
<resources>

</resources>
        """.trimIndent()
        Assert.assertEquals(expected, XmlPrinterScanner().start(resource))
    }

    @Test
    fun shouldGenerateXmlContentWithStringArray() {
        val resource = Resource(arrayListOf(
                StringsArrayTranslation("android", arrayListOf(
                        StringTranslation("", "KitKat", "test"),
                        StringTranslation("", "Lollipop", "test"),
                        StringTranslation("", "Marshmallow", "test")
                ), "test")
        ))
        val expected = """
<?xml version="1.0" encoding="UTF-8"?>
<resources>
	<string-array name="android">
		<item>KitKat</item>
		<item>Lollipop</item>
		<item>Marshmallow</item>
	</string-array>
</resources>
        """.trimIndent()
        Assert.assertEquals(expected, XmlPrinterScanner().start(resource))
    }
}