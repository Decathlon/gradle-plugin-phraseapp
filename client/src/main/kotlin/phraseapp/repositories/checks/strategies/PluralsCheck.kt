package phraseapp.repositories.checks.strategies

import kotlinx.coroutines.coroutineScope
import phraseapp.extensions.ResourceTranslation
import phraseapp.repositories.checks.CheckTranslation
import phraseapp.repositories.checks.CheckType

suspend fun ResourceTranslation.pluralsCheck(
    targetContent: ResourceTranslation
): List<CheckTranslation> = coroutineScope {
    val list = arrayListOf<CheckTranslation>()
    this@pluralsCheck.plurals.forEach { defaultTranslation ->
        val equivalent = targetContent.plurals.firstOrNull { it.key == defaultTranslation.key }
        if (equivalent != null && defaultTranslation.plurals.size > equivalent.plurals.size) {
            list.add(CheckTranslation(equivalent.key, CheckType.PLURALS))
        }
    }
    return@coroutineScope list
}