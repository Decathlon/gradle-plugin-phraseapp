package phraseapp.repositories.checks

import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.repositories.checks.CheckType.PLACEHOLDER
import phraseapp.repositories.checks.CheckType.PLURALS

interface CheckRepository {

    suspend fun check(checkTypes: List<CheckType> = arrayListOf(PLURALS, PLACEHOLDER))

    companion object {
        fun newInstance(
            baseUrl: String, buildDir: String, localeRegex: String, token: String, projectId: String, platform: Platform
        ): CheckRepository = CheckRepositoryImpl(
            buildDir,
            FileOperationImpl(),
            localeRegex,
            PhraseAppNetworkDataSource.newInstance(baseUrl, token, projectId, platform.format),
            platform
        )
    }
}

data class CheckLocaleError(val locale: String, val translations: List<CheckTranslation>)
data class CheckTranslation(val key: String, val type: CheckType)
enum class CheckType { PLURALS, PLACEHOLDER }