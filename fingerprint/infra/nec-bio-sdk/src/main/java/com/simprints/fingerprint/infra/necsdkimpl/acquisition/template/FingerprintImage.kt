package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

/**
 * An already processed fingerprint image
 * This class stores the image bytes and the image metadata
 * This image is ready to be used for template extraction
 */
@Suppress("ArrayInDataClass") // Suppressed because there is no need to implement equals and hashcode for this data class
internal data class FingerprintImage(
    val imageBytes: ByteArray,
    val width: Int,
    val height: Int,
    val resolution: Int,
)
