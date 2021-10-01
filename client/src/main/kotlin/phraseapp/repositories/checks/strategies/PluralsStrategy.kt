package phraseapp.repositories.checks.strategies

import kotlinx.coroutines.coroutineScope
import phraseapp.extensions.ResourceTranslation
import phraseapp.repositories.checks.CheckTranslation
import phraseapp.repositories.checks.CheckType

class PluralsStrategy : Strategy {
    override suspend fun apply(
        defaultContent: ResourceTranslation, targetContent: ResourceTranslation
    ): List<CheckTranslation> = coroutineScope {
        val list = arrayListOf<CheckTranslation>()
        defaultContent.plurals.forEach { defaultTranslation ->
            val equivalent = targetContent.plurals.firstOrNull { it.key == defaultTranslation.key }
            if (equivalent != null && defaultTranslation.plurals.size > equivalent.plurals.size) {
                list.add(CheckTranslation(equivalent.key, CheckType.PLURALS))
            }
        }
        return@coroutineScope list
    }
}