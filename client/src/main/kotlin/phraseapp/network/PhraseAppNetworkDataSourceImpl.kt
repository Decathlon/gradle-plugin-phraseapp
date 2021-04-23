package phraseapp.network

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody.FORM
import okhttp3.MultipartBody.Part
import okhttp3.RequestBody
import java.io.File
import java.util.regex.Pattern

private class LocaleResponse internal constructor(internal val locale: Locale, internal val content: String)

class PhraseAppNetworkDataSourceImpl(
        private val token: String,
        private val projectId: String,
        private val fileFormat: String,
        private val service: PhraseAppService) : PhraseAppNetworkDataSource {

    override fun downloadAllLocales(overrideDefaultFile: Boolean, exceptions: Map<String, String>, placeHolder: Boolean, localeNameRegex: String): Single<Map<String, LocaleContent>> {
        val namePattern = Pattern.compile(localeNameRegex)
        return service.getLocales(token, projectId)
                .toObservable()
                .flatMapIterable { it }
                .filter { it.isDefault.not() or overrideDefaultFile }
                .flatMapSingle { locale ->
                    service.download(token, projectId, locale.id, fileFormat, placeHolder).map { LocaleResponse(locale, it.string()) }
                }
                .toMap({
                    val phraseAppLocale = it.locale
                    val matcher = namePattern.matcher(phraseAppLocale.name)
                    val code = if (matcher.matches() && matcher.groupCount() == 1) matcher.group(1) else phraseAppLocale.code
                    if (exceptions.containsKey(code)) exceptions[code]
                    else code
                }, { LocaleContent(it.content, it.locale.isDefault) })
    }

    override fun upload(localeId: String, filePath: String): Completable {
        val file = File(filePath)
        val requestFile = RequestBody.create(MediaType.parse("text/plain"), file)
        val body = Part.createFormData("file", file.name, requestFile)
        val locale = RequestBody.create(FORM, localeId)
        val fileFormat = RequestBody.create(FORM, fileFormat)
        val updateTranslation = RequestBody.create(FORM, "true")
        val updateDescription = RequestBody.create(FORM, "true")
        val skipUploadTag = RequestBody.create(FORM, "true")
        return service.upload(token, projectId, body, locale, fileFormat, updateTranslation, updateDescription, skipUploadTag).ignoreElement()
    }
}
