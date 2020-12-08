package com.simprints.id.activities.login.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class QrCodeResponse(
    val projectId: String,
    val projectSecret: String,
    @JsonProperty("backend") val apiBaseUrl: String? = null
)
