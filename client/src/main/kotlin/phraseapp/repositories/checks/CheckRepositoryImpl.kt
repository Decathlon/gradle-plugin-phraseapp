package phraseapp.repositories.checks

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import phraseapp.extensions.parse
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.repositories.checks.strategies.placeholderCheck
import phraseapp.repositories.checks.strategies.pluralsCheck
import java.io.File

class CheckRepositoryImpl(
    private val buildDir: String,
    private val fileOperation: FileOperation = FileOperationImpl(),
    private val localeRegex: String,
    private val phraseAppNetworkDataSource: PhraseAppNetworkDataSource,
    private val platform: Platform
) : CheckRepository {

    override suspend fun check(checkTypes: List<CheckType>) {
        val checkLocaleErrors =
            checkTypes.map { check(it) }.flatten().groupBy { error -> error.locale }.values.flatten()

        if (checkLocaleErrors.isNotEmpty()) {
            fileOperation.print(
                "$buildDir${File.separator}errors.txt",
                checkLocaleErrors.joinToString("\n") { error ->
                    error.translations.map { "${error.locale} :: ${it.type} :: ${it.key}" }
                        .joinToString("\n")
                }
            )
            throw ChecksException(checkLocaleErrors)
        }
    }

    private suspend fun check(checkType: CheckType): List<CheckLocaleError> = coroutineScope {
        val localesContent = phraseAppNetworkDataSource.downloadAllLocales(true, emptyMap(), true, localeRegex)
        val defaultContent = localesContent.values.first { it.isDefault }.content.parse(platform.format)
        val targetsContent = localesContent.entries
            .filter { it.value.isDefault.not() }
            .associate { Pair(it.key, it.value.content.parse(platform.format)) }

        return@coroutineScope async {
            return@async targetsContent.entries
                .map { target ->
                    CheckLocaleError(
                        locale = target.key,
                        translations = when (checkType) {
                            CheckType.PLURALS -> defaultContent.pluralsCheck(target.value)
                            CheckType.PLACEHOLDER -> defaultContent.placeholderCheck(target.value)
                        }
                    )
                }
                .filter { it.translations.isNotEmpty() }
        }.await()
    }
}

class ChecksException(val errors: List<CheckLocaleError>) : Throwable()