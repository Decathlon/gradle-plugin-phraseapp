package phraseapp.network.mock

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import phraseapp.network.Locale
import phraseapp.network.PhraseAppService
import java.io.File

class MockPhraseAppService : PhraseAppService {
    override fun getLocales(authorization: String, projectId: String): Single<List<Locale>> {
        return Single.just(Gson().fromJson(File("src/test/resources/phraseapp/locales.json").readText()))
    }

    override fun download(authorization: String, projectId: String, localeId: String, fileFormat: String, placeHolder: Boolean): Single<ResponseBody> {
        val file = when (localeId) {
            "a45eaf4f57ad2b51d228710d34ec43d2" -> File("src/test/resources/android-remote/values/strings.xml")
            "8430898e9e9d7deb64979ac01d914041" -> File("src/test/resources/android-remote/values-fr-rFR/strings.xml")
            "8400891e9e9dfdeb6d979ac01d9jd087" -> File("src/test/resources/android-remote/values-es-rES/strings.xml")
            else -> throw NoSuchElementException()
        }
        return Single.just(ResponseBody.create(MediaType.parse("application/json"), file.readText()))
    }

    override fun upload(authorization: String, projectId: String, file: MultipartBody.Part, localeId: RequestBody, fileFormat: RequestBody, updateTranslations: RequestBody, updateDescriptions: RequestBody, skipUploadTags: RequestBody): Single<ResponseBody> {
        return Single.just(ResponseBody.create(MediaType.parse("text/xml"), ""))
    }
}

private inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)!!
