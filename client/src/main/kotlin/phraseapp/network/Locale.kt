package phraseapp.network

import com.google.gson.annotations.SerializedName

data class Locale(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("default") val isDefault: Boolean,
    @SerializedName("main") val main: Boolean,
    @SerializedName("rtl") val rtl: Boolean,
    @SerializedName("plurals_form") val pluralsForm: List<String>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
