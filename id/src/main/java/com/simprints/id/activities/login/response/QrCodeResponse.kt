package com.simprints.id.activities.login.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class QrCodeResponse(
    val projectId: String,
    val projectSecret: String,
    @SerializedName("backend") val apiBaseUrl: String? = null
)
