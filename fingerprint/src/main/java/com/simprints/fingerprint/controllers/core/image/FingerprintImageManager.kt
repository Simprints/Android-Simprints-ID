package com.simprints.fingerprint.controllers.core.image

interface FingerprintImageManager {

    suspend fun save(imageBytes: ByteArray): FingerprintImageRef
}
