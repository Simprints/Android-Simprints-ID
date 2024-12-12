package com.simprints.feature.login.screens.qrscanner

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
internal data class QrCodeContent(
    val projectId: String,
    val projectSecret: String,
    @JsonProperty("backend") val apiBaseUrl: String? = null,
)
