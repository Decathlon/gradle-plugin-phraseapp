package phraseapp.repositories.checks

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import phraseapp.extensions.parse
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.network.PhraseAppNetworkDataSource
import phraseapp.repositories.checks.strategies.Strategy
import java.io.File

class CheckRepositoryImpl(
    private val buildDir: String,
    private val fileOperation: FileOperation = FileOperationImpl(),
    private val localeRegex: String,
    private val phraseAppNetworkDataSource: PhraseAppNetworkDataSource,
    private val platform: Platform
) : CheckRepository {
    override suspend fun check(strategies: List<Strategy>) = coroutineScope {
        val localesContent = phraseAppNetworkDataSource.downloadAllLocales(true, emptyMap(), true, localeRegex)
        val defaultContent = localesContent.values.first { it.isDefault }.content.parse(platform.format)
        val targetsContent = localesContent.entries
            .filter { it.value.isDefault.not() }
            .associate { Pair(it.key, it.value.content.parse(platform.format)) }
        val errors = strategies
            .map { strategy ->
                async {
                    return@async targetsContent.entries
                        .map { target -> CheckLocaleError(target.key, strategy.apply(defaultContent, target.value)) }
                        .filter { it.translations.isNotEmpty() }
                }
            }
            .awaitAll()
            .flatten().groupBy { error -> error.locale }.values.flatten()
        if (errors.isNotEmpty()) {
            fileOperation.print(
                "$buildDir${File.separator}errors.txt",
                errors.map { error ->
                    error.translations.map { "${error.locale} :: ${it.type} :: ${it.key}" }
                        .joinToString("\n")
                }.joinToString("\n")
            )
            throw ChecksException(errors)
        }
        return@coroutineScope
    }
}

class ChecksException(val errors: List<CheckLocaleError>) : Throwable()