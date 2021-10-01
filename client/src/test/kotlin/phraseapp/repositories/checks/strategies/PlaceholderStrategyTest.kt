package phraseapp.repositories.checks.strategies

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import phraseapp.extensions.parse
import phraseapp.internal.platforms.Android
import phraseapp.repositories.checks.CheckType
import java.io.File

class PlaceholderStrategyTest {
    @Test
    fun shouldDetectWhenThereIsMalFormedPlaceHolders() = runBlocking {
        val default = File("src/test/resources/android-errors/values/strings.xml").readText().parse(Android.format)
        val target = File("src/test/resources/android-errors/values-fr-rFR/strings.xml").readText().parse(Android.format)

        val strategy = PlaceholderStrategy()
        val errors = strategy.apply(default, target)
        assertEquals(2, errors.size)
        assertEquals("hello", errors[0].key)
        assertEquals(CheckType.PLACEHOLDER, errors[0].type)
        assertEquals("numberOfSongsAvailable", errors[1].key)
        assertEquals(CheckType.PLACEHOLDER, errors[1].type)
    }

    @Test
    fun shouldNotDetectPlaceHolderErrorWhenThereIsNoPlaceHolderError() = runBlocking {
        val default = File("src/test/resources/android-local/values/strings.xml").readText().parse(Android.format)
        val target = File("src/test/resources/android-local/values-es-rES/strings.xml").readText().parse(Android.format)

        val strategy = PlaceholderStrategy()
        val errors = strategy.apply(default, target)
        assertEquals(0, errors.size)
    }
}