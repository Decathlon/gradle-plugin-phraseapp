package phraseapp.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface PhraseAppService {
    @GET("v2/projects/{project_id}/locales?per_page=100")
    suspend fun getLocales(
        @Header("Authorization") authorization: String,
        @Path("project_id") projectId: String
    ): List<Locale>

    @GET("v2/projects/{project_id}/locales/{locale_id}/download")
    suspend fun download(
        @Header("Authorization") authorization: String,
        @Path("project_id") projectId: String,
        @Path("locale_id") localeId: String,
        @Query("file_format") fileFormat: String,
        @Query("format_options[convert_placeholder]") placeHolder: Boolean
    ): ResponseBody

    @Multipart
    @POST("v2/projects/{project_id}/uploads")
    suspend fun upload(
        @Header("Authorization") authorization: String,
        @Path("project_id") projectId: String,
        @Part file: MultipartBody.Part,
        @Part("locale_id") localeId: RequestBody,
        @Part("file_format") fileFormat: RequestBody,
        @Part("update_translations") updateTranslations: RequestBody,
        @Part("update_descriptions") updateDescriptions: RequestBody,
        @Part("skip_upload_tags") skipUploadTags: RequestBody
    ): ResponseBody
}
