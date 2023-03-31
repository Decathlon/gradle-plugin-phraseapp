package phraseapp.network

import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody.Companion.FORM
import okhttp3.MultipartBody.Part
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.regex.Pattern

private class LocaleResponse(val locale: Locale, val content: String)

const val CONCURRENT_LIMIT = 4
const val THROTTLING_LIMIT = 500L

class PhraseAppNetworkDataSourceImpl(
    private val token: String,
    private val projectId: String,
    private val fileFormat: String,
    private val service: PhraseAppService
) : PhraseAppNetworkDataSource {

    override suspend fun downloadAllLocales(
        overrideDefaultFile: Boolean,
        exceptions: Map<String, String>,
        placeHolder: Boolean,
        localeNameRegex: String,
        allowedLocaleCodes: List<String>
    ): Map<String, LocaleContent> = coroutineScope {
        val namePattern = Pattern.compile(localeNameRegex)
        val locales = service.getLocales(token, projectId)
            .filter {
                (it.isDefault.not() or overrideDefaultFile) &&
                        (allowedLocaleCodes.isEmpty() || allowedLocaleCodes.contains(it.code))
            }
        val chunked = locales.chunked(CONCURRENT_LIMIT)
        val localesResponse = iterative(chunked, THROTTLING_LIMIT, placeHolder)
        return@coroutineScope localesResponse
            .associate {
                val phraseAppLocale = it.locale
                val matcher = namePattern.matcher(phraseAppLocale.name)
                val code =
                    if (matcher.matches() && matcher.groupCount() == 1) matcher.group(1) else phraseAppLocale.code
                val key = exceptions.getOrDefault(code, code)
                key to LocaleContent(it.content, it.locale.isDefault)
            }
    }

    private suspend fun iterative(
        list: List<List<Locale>>,
        onePerMillis: Long,
        placeHolder: Boolean
    ) = coroutineScope<List<LocaleResponse>> {
        val target = arrayListOf<LocaleResponse>()
        for (item in list) {
            target.addAll(
                item
                    .map { locale ->
                        async {
                            val file = service.download(token, projectId, locale.id, fileFormat, placeHolder)
                            return@async LocaleResponse(locale, file.string())
                        }
                    }
                    .awaitAll()
            )
            delay(onePerMillis)
        }
        return@coroutineScope target
    }

    override suspend fun upload(localeId: String, filePath: String) = coroutineScope {
        val file = File(filePath)
        val requestFile = file.asRequestBody("text/plain".toMediaTypeOrNull())
        val body = Part.createFormData("file", file.name, requestFile)
        val locale = localeId.toRequestBody(FORM)
        val fileFormat = fileFormat.toRequestBody(FORM)
        val updateTranslation = "true".toRequestBody(FORM)
        val updateDescription = "true".toRequestBody(FORM)
        val skipUploadTag = "true".toRequestBody(FORM)
        service.upload(
            token,
            projectId,
            body,
            locale,
            fileFormat,
            updateTranslation,
            updateDescription,
            skipUploadTag
        )
        return@coroutineScope
    }
}
