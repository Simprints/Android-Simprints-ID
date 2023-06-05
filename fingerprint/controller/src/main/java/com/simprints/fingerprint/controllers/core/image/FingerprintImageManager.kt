package com.simprints.fingerprint.controllers.core.image

import com.simprints.fingerprint.data.domain.images.FingerprintImageRef

/**
 * This interface manages fingerprint images
 */
interface FingerprintImageManager {

    /**
     * This function save a fingerprint image and returns a reference to its storage location
     *
     * @param imageBytes  the bytes of the fingerprint image
     * @param captureEventId  the unique id of the capture event
     * @param fileExtension  the extension of the file the image will be written into
     */
    suspend fun save(imageBytes: ByteArray, captureEventId: String, fileExtension: String): FingerprintImageRef?
}
