package phraseapp.repositories.checks.strategies

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import phraseapp.extensions.ResourceTranslation
import phraseapp.repositories.checks.CheckTranslation
import phraseapp.repositories.checks.CheckType

class PlaceholderStrategy : Strategy {
    override fun apply(defaultContent: ResourceTranslation, targetContent: ResourceTranslation): Single<List<CheckTranslation>> {
        val strings = Observable.just(defaultContent, targetContent).toList().flatMap {
            val list = arrayListOf<CheckTranslation>()
            defaultContent.strings.forEach { default ->
                val defaultPlaceHolderCount = default.value.countPlaceHolder()
                if (defaultPlaceHolderCount > 0) {
                    val equivalent = targetContent.strings.firstOrNull { it.key == default.key }
                    if (equivalent != null && equivalent.value.countPlaceHolder() != defaultPlaceHolderCount) {
                        list.add(CheckTranslation(equivalent.key, CheckType.PLACEHOLDER))
                    }
                }
            }
            return@flatMap Single.just(list)
        }
        val plurals = Observable.just(defaultContent, targetContent).toList().flatMap {
            val list = arrayListOf<CheckTranslation>()
            defaultContent.plurals.forEach { default ->
                default.plurals.forEach { plural ->
                    val defaultPlaceHolderCount = plural.value.countPlaceHolder()
                    if (defaultPlaceHolderCount > 0) {
                        val pluralsEquivalent = targetContent.plurals.firstOrNull { it.key == default.key }
                        val pluralEquivalent = pluralsEquivalent?.plurals?.firstOrNull { it.key == plural.key }
                        if (pluralEquivalent != null && pluralEquivalent.value.countPlaceHolder() != defaultPlaceHolderCount) {
                            list.add(CheckTranslation(pluralsEquivalent.key, CheckType.PLACEHOLDER))
                        }
                    }
                }
            }
            return@flatMap Single.just(list)
        }
        return Single.zip(strings, plurals, BiFunction { stringsRes, pluralsRes -> stringsRes + pluralsRes })
    }
}

private fun String.countPlaceHolder(): Int = hasPlaceHolderRegex.findAll(this).count()
private val hasPlaceHolderRegex = "%([0-9]+\\$)?[sd]".toRegex()