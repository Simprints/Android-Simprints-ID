package com.simprints.feature.login.screens.qrscanner

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class QrCodeContent(
    val projectId: String,
    val projectSecret: String,
    @SerialName("backend") val apiBaseUrl: String? = null,
)
