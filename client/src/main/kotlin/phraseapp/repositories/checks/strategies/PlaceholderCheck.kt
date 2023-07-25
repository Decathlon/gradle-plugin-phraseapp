package phraseapp.repositories.checks.strategies

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import phraseapp.extensions.ResourceTranslation
import phraseapp.repositories.checks.CheckTranslation
import phraseapp.repositories.checks.CheckType

suspend fun ResourceTranslation.placeholderCheck(
    targetContent: ResourceTranslation
): List<CheckTranslation> = coroutineScope {
    val strings = async {
        val strings = arrayListOf<CheckTranslation>()
        this@placeholderCheck.strings.forEach { default ->
            val defaultPlaceHolderCount = default.value.countPlaceHolder()
            if (defaultPlaceHolderCount > 0) {
                val equivalent = targetContent.strings.firstOrNull { it.key == default.key }
                if (equivalent != null && equivalent.value.countPlaceHolder() != defaultPlaceHolderCount) {
                    strings.add(CheckTranslation(equivalent.key, CheckType.PLACEHOLDER))
                }
            }
        }
        return@async strings
    }

    val plurals = async {
        val plurals = arrayListOf<CheckTranslation>()
        this@placeholderCheck.plurals.forEach { default ->
            default.plurals.forEach { plural ->
                val defaultPlaceHolderCount = plural.value.countPlaceHolder()
                if (defaultPlaceHolderCount > 0) {
                    val pluralsEquivalent = targetContent.plurals.firstOrNull { it.key == default.key }
                    val pluralEquivalent = pluralsEquivalent?.plurals?.firstOrNull { it.key == plural.key }
                    if (pluralEquivalent != null && pluralEquivalent.value.countPlaceHolder() != defaultPlaceHolderCount) {
                        plurals.add(CheckTranslation(pluralsEquivalent.key, CheckType.PLACEHOLDER))
                    }
                }
            }
        }
        return@async plurals
    }

    return@coroutineScope strings.await() + plurals.await()
}

private fun String.countPlaceHolder(): Int = hasPlaceHolderRegex.findAll(this).count()
private val hasPlaceHolderRegex = "%([0-9]+\\$)?[sd]".toRegex()