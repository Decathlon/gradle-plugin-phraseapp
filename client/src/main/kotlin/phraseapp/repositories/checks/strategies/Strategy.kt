package phraseapp.repositories.checks.strategies

import io.reactivex.Single
import phraseapp.extensions.ResourceTranslation
import phraseapp.repositories.checks.CheckTranslation

interface Strategy {
    fun apply(defaultContent: ResourceTranslation, targetContent: ResourceTranslation): Single<List<CheckTranslation>>
}