package phraseapp.repositories.checks.strategies

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import phraseapp.extensions.parse
import phraseapp.internal.platforms.Android
import phraseapp.repositories.checks.CheckType
import java.io.File

class PluralsStrategyTest {
    @Test
    fun shouldDetectWhenThereIsNoAllTranslationsForAPlural() = runBlocking {
        val default = File("src/test/resources/android-errors/values/strings.xml").readText().parse(Android.format)
        val target = File("src/test/resources/android-errors/values-fr-rFR/strings.xml").readText().parse(Android.format)

        val strategy = PluralsStrategy()
        val errors = strategy.apply(default, target)
        assertEquals(1, errors.size)
        assertEquals("numberOfSongsAvailable", errors[0].key)
        assertEquals(CheckType.PLURALS, errors[0].type)
    }

    @Test
    fun shouldNotDetectPluralsErrorWhenThereIsNoPluralsError() = runBlocking {
        val default = File("src/test/resources/android-local/values/strings.xml").readText().parse(Android.format)
        val target = File("src/test/resources/android-local/values-es-rES/strings.xml").readText().parse(Android.format)

        val strategy = PluralsStrategy()
        val errors = strategy.apply(default, target)
        assertEquals(0, errors.size)
    }
}