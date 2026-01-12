package com.simprints.infra.images.remote.signedurl.api

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiSampleUploadUrlResponse(
    val url: String,
    val sampleId: String,
)
