package phraseapp.repositories.checks

import io.reactivex.Completable
import io.reactivex.Observable
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
    override fun check(strategies: List<Strategy>): Completable {
        return phraseAppNetworkDataSource.downloadAllLocales(true, emptyMap(), true, localeRegex)
                .flatMap { localesContent ->
                    val defaultContent = localesContent.values.first { it.isDefault }.content.parse(platform.format)
                    val targetsContent = localesContent.entries
                            .filter { it.value.isDefault.not() }
                            .associate { Pair(it.key, it.value.content.parse(platform.format)) }
                    return@flatMap Observable.fromIterable(strategies)
                            .flatMapSingle { strategy ->
                                Observable.fromIterable(targetsContent.entries).flatMap { target ->
                                    strategy.apply(defaultContent, target.value).toObservable()
                                            .map { CheckLocaleError(target.key, it) }
                                }.filter { it.translations.isNotEmpty() }.toList()
                            }
                            .toList()
                            .map { it.flatten().groupBy { error -> error.locale }.values.flatten() }
                }
                .doOnSuccess { errors ->
                    if (errors.isNotEmpty()) {
                        fileOperation.print(
                                "$buildDir${File.separator}errors.txt",
                                errors.map { error ->
                                    error.translations.map { "${error.locale} :: ${it.type} :: ${it.key}" }
                                            .joinToString("\n")
                                }.joinToString("\n")
                        )
                    }
                }
                .flatMapCompletable {
                    if (it.isNotEmpty()) {
                        throw ChecksException(it)
                    }
                    return@flatMapCompletable Completable.complete()
                }
    }
}

class ChecksException(val errors: List<CheckLocaleError>) : Throwable()