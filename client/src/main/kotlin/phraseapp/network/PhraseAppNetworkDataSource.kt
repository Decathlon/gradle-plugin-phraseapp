package phraseapp.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


const val DEFAULT_REGEX = ".+_([a-z]{2}-[A-Z]{2})"
const val DEFAULT_PLACEHOLDER = false
val DEFAULT_EXCEPTIONS: Map<String, String> = emptyMap()
const val DEFAULT_OVERRIDE_DEFAULT_FILE = false
const val PHRASEAPP_BASEURL = "https://api.phrase.com/api/"

data class LocaleContent(val content: String, val isDefault: Boolean)

interface PhraseAppNetworkDataSource {
    fun downloadAllLocales(
            overrideDefaultFile: Boolean = DEFAULT_OVERRIDE_DEFAULT_FILE,
            exceptions: Map<String, String> = DEFAULT_EXCEPTIONS,
            placeHolder: Boolean = DEFAULT_PLACEHOLDER,
            localeNameRegex: String = DEFAULT_REGEX
    ): Single<Map<String, LocaleContent>>

    fun upload(localeId: String, filePath: String): Completable

    companion object {
        fun newInstance(baseUrl: String, token: String, projectId: String, fileFormat: String): PhraseAppNetworkDataSource {
            val gson = GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create()
            val client = OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .hostnameVerifier { _, _ -> true }
                    .build()
            val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            return PhraseAppNetworkDataSourceImpl(token, projectId, fileFormat, retrofit.create(PhraseAppService::class.java))
        }
    }
}
