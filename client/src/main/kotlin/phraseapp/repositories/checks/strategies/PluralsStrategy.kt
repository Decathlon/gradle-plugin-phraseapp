package phraseapp.repositories.checks.strategies

import io.reactivex.Observable
import io.reactivex.Single
import phraseapp.extensions.ResourceTranslation
import phraseapp.repositories.checks.CheckTranslation
import phraseapp.repositories.checks.CheckType

class PluralsStrategy : Strategy {
    override fun apply(defaultContent: ResourceTranslation, targetContent: ResourceTranslation): Single<List<CheckTranslation>> {
        return Observable.just(defaultContent, targetContent).toList().flatMap { resources ->
            val default = resources[0]
            val target = resources[1]
            val list = arrayListOf<CheckTranslation>()
            default.plurals.forEach { defaultTranslation ->
                val equivalent = target.plurals.firstOrNull { it.key == defaultTranslation.key }
                if (equivalent != null && defaultTranslation.plurals.size > equivalent.plurals.size) {
                    list.add(CheckTranslation(equivalent.key, CheckType.PLURALS))
                }
            }
            Single.just(list)
        }
    }
}