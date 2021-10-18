package phraseapp.extensions

import org.junit.Assert.assertEquals
import org.junit.Test
import phraseapp.internal.platforms.Android
import phraseapp.internal.xml.XmlPrinterScanner
import java.io.File

class StringParsingExtensionTest {
    @Test
    fun shouldParseXmlWithSimpleItem() {
        val xml = File("src/test/resources/android/values-fr-rFR/strings.xml").readText().parse(Android.format)
        assertEquals(xml.plurals.size, 0)
        assertEquals(xml.strings.size, 1)
        assertEquals(xml.strings[0].key, "hello")
        assertEquals(xml.strings[0].value, "Hello!")
    }

    @Test
    fun shouldParseArbWithSimpleItem() {
        val file = File("src/test/resources/flutter/values/strings_fr.arb")
        val arb = file.readText().parse(file)
        assertEquals(arb.plurals.size, 0)
        assertEquals(arb.strings.size, 1)
        assertEquals(arb.strings[0].key, "hello")
        assertEquals(arb.strings[0].value, "Hello!")
    }

    @Test
    fun shouldParseXmlWithPlurals() {
        val xml = File("src/test/resources/android/values/strings.xml").readText().parse(Android.format)
        assertEquals(xml.strings.size, 0)
        assertEquals(xml.plurals.size, 1)
        assertEquals(xml.plurals[0].key, "numberOfSongsAvailable")
        assertEquals(xml.plurals[0].plurals.size, 2)
        assertEquals(xml.plurals[0].plurals[0].key, "one")
        assertEquals(xml.plurals[0].plurals[0].value, "%d song found.")
        assertEquals(xml.plurals[0].plurals[1].key, "other")
        assertEquals(xml.plurals[0].plurals[1].value, "%d songs found.")
    }

    @Test
    fun shouldParseXmlWithComments() {
        val xml = File("src/test/resources/android-comments/values/strings.xml").readText().parse(Android.format)
        assertEquals(xml.strings.size, 1)
        assertEquals(xml.strings[0].comment.text, """
[ACCOUNT FORM] For CO country : Cedula.
Type : Label
        """.trimIndent())
        assertEquals(xml.plurals[0].comment.text, "Dynamic creation account - title for surname field")
        assertEquals(xml.plurals[0].plurals[0].comment.text, """
[ACCOUNT FORM] For CO country : Cedula.
Type : Label
        """.trimIndent())
        assertEquals(xml.plurals[0].plurals[1].comment.text, "Dynamic creation account - title for surname field")
    }

    @Test
    fun shouldParseXmlWithTags() {
        val originalText = File("src/test/resources/android-tags/values/strings.xml").readText()
        val xmlObject = originalText.parse(Android.format)
        assertEquals("Hey<br/><br/><b>Oh</b><br/><br/>Hey<b>Oh</b>", xmlObject.strings[0].value)
        assertEquals("<![CDATA[Blabla]]>", xmlObject.strings[1].value)
        assertEquals("Picking of {0,choice,1#1 article|1<{0} articles}", xmlObject.strings[2].value)
        assertEquals("Click&amp;Collect &lt;", xmlObject.strings[3].value)

        val xmlPrinted = (Android.printer as XmlPrinterScanner).start(xmlObject.toResource())
        assertEquals(originalText, xmlPrinted)
    }
}