package phraseapp.network.mock

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import phraseapp.network.Locale
import phraseapp.network.PhraseAppService
import java.io.File

class MockPhraseAppService : PhraseAppService {
    override suspend fun getLocales(authorization: String, projectId: String): List<Locale> =
        Gson().fromJson(File("src/test/resources/phraseapp/locales.json").readText())

    override suspend fun download(
        authorization: String,
        projectId: String,
        localeId: String,
        fileFormat: String,
        placeHolder: Boolean
    ): ResponseBody {
        val file = when (localeId) {
            "a45eaf4f57ad2b51d228710d34ec43d2" -> File("src/test/resources/android-remote/values/strings.xml")
            "8430898e9e9d7deb64979ac01d914041" -> File("src/test/resources/android-remote/values-fr-rFR/strings.xml")
            "8400891e9e9dfdeb6d979ac01d9jd087" -> File("src/test/resources/android-remote/values-es-rES/strings.xml")
            else -> throw NoSuchElementException()
        }
        return file.readText().toResponseBody("application/json".toMediaTypeOrNull())
    }

    override suspend fun upload(
        authorization: String,
        projectId: String,
        file: MultipartBody.Part,
        localeId: RequestBody,
        fileFormat: RequestBody,
        updateTranslations: RequestBody,
        updateDescriptions: RequestBody,
        skipUploadTags: RequestBody
    ): ResponseBody = "".toResponseBody("text/xml".toMediaTypeOrNull())
}

private inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)!!
