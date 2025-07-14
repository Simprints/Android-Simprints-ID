package com.simprints.infra.images.remote.signedurl

import com.simprints.infra.images.model.SecuredImageRef

internal data class SampleUploadData(
    val imageRef: SecuredImageRef,
    val sampleId: String,
    val sessionId: String,
    val modality: String,
    val md5: String,
    val size: Long,
    val metadata: Map<String, String>,
)
