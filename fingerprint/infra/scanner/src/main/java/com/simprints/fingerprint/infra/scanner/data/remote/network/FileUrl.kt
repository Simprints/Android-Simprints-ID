package com.simprints.fingerprint.infra.scanner.data.remote.network

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class FileUrl(
    val url: String,
)
