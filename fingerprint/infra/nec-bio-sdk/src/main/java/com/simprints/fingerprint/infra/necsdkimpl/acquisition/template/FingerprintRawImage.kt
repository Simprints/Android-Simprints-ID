package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

const val DEFAULT_RESOLUTION: Short = 500

/**
 * Fingerprint raw uncompressed image
 * This class stores the raw image bytes and the image metadata
 */
//Suppressed because there is no need to implement equals and hashcode for this data class
@Suppress("ArrayInDataClass")
data class FingerprintRawImage(
    val un20SerialNumber: ByteArray,
    val imageBytes: ByteArray,
    val width: Int,
    val height: Int,
    val brightness: Byte,
    val resolution: Int = DEFAULT_RESOLUTION.toInt(),
)
