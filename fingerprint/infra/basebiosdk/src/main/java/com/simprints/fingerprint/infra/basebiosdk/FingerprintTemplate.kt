package com.simprints.fingerprint.infra.basebiosdk

data class FingerprintTemplate(
    val templateFormat: String,
    val template: ByteArray
)
