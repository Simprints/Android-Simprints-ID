package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

const val DEFAULT_RESOLUTION: Short = 500

/**
 * Fingerprint raw non compressed image
 *
 * @property imageBytes
 * @property width
 * @property height
 * @property resolution
 * @constructor Create empty Fingerprint raw image
 */
// Suppresses the "ArrayInDataClass" warning because there is no need to override equals for this class
@Suppress("ArrayInDataClass")
data class FingerprintRawImage(
    val un20SerialNumber: ByteArray,
    val imageBytes: ByteArray,
    val width: Int,
    val height: Int,
    val brightness: Byte,
    val resolution: Int = DEFAULT_RESOLUTION.toInt(),
)
