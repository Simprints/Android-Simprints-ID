package com.simprints.infra.images.remote.signedurl.api

import androidx.annotation.Keep

@Keep
internal data class ApiSampleUploadUrlResponse(
    val url: String,
    val sampleId: String,
)
