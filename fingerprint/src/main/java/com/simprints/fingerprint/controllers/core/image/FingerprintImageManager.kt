package com.simprints.fingerprint.controllers.core.image

import com.simprints.fingerprint.data.domain.images.FingerprintImageRef

interface FingerprintImageManager {

    suspend fun save(imageBytes: ByteArray, captureEventId: String, fileExtension: String): FingerprintImageRef?
}
