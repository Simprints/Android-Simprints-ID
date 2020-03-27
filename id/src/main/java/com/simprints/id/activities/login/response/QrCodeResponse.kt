package com.simprints.id.activities.login.response

import com.google.gson.annotations.SerializedName

data class QrCodeResponse(
    val projectId: String,
    val projectSecret: String,
    @SerializedName("backend") val apiBaseUrl: String? = null
)
