package phraseapp.repositories.checks.strategies

import phraseapp.extensions.ResourceTranslation
import phraseapp.repositories.checks.CheckTranslation

interface Strategy {
    suspend fun apply(defaultContent: ResourceTranslation, targetContent: ResourceTranslation): List<CheckTranslation>
}