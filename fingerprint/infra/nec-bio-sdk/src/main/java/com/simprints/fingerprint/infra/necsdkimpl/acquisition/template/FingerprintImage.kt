package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

data class FingerprintImage(
    val imageBytes: ByteArray,
    val width: Int,
    val height: Int,
    val resolution: Int
)
