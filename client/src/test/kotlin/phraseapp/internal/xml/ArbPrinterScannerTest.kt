package phraseapp.internal.xml

import org.junit.Assert
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class ArbPrinterScannerTest {

    @Test
    fun shouldGenerateArbContentWithString() {
        val resource = Resource(arrayListOf(
                StringTranslation("test", "Test", "test")
        ))
        val printer = ArbPrinterScanner()
        val result = printer.start(resource)
        val dateStr = printer.getDate()
        val expected = """{
	"@@last_modified":"$dateStr",
	"test":"Test"
}"""
        Assert.assertEquals(expected, result)
    }

    @Test
    fun shouldGenerateArbContentWithMultipleString() {
        val resource = Resource(arrayListOf(
                StringTranslation("test", "Test", "test"),
                StringTranslation("one", "One", "test")
        ))
        val printer = ArbPrinterScanner()
        val result = printer.start(resource)
        val dateStr = printer.getDate()
        val expected = """{
	"@@last_modified":"$dateStr",
	"test":"Test",
	"one":"One"
}"""
        Assert.assertEquals(expected, result)
    }
}