package com.simprints.fingerprint.infra.basebiosdk.acquization.domain

class TemplateResponse<T>(
    val template: ByteArray,
    val templateMetadata:T?
)
