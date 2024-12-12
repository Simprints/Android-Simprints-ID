package com.simprints.fingerprint.infra.basebiosdk.acquisition.domain

class TemplateResponse<T>(
    val template: ByteArray,
    val templateMetadata: T?,
)
