package com.simprints.fingerprint.infra.scanner.domain.fingerprint

/**
 * Raw unprocessed image
 *
 *
 * @property imageBytes
 *
 */
class RawUnprocessedImage(
    private val imageBytes: ByteArray,
) {
    val imageData
        get() = with(imageBytes) { copyOfRange(IMAGE_HEADER_SIZE, imageBytes.size) }
    val brightness
        get() = imageBytes[BRIGHTNESS_INDEX]
    val un20SerialNumber
        get() = imageBytes.copyOfRange(0, UN20_SERIAL_SIZE)

    companion object {
        private const val UN20_SERIAL_SIZE = 15
        private const val BRIGHTNESS_INDEX = 15
        private const val IMAGE_HEADER_SIZE = 20
    }
}
