package com.simprints.infra.images.remote.signedurl.api

import androidx.annotation.Keep

@Keep
internal data class ApiSampleUploadUrlRequest(
    val sampleId: String,
    val sessionId: String,
    val md5: String,
    val modality: String,
    val metadata: Map<String, String>,
)
