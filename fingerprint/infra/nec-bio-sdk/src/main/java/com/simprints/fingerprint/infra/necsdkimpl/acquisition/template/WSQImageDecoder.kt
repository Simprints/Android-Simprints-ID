package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.gemalto.wsq.WSQDecoder
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.RawUnprocessedImage
import com.ygoular.bitmapconverter.BitmapConverter
import com.ygoular.bitmapconverter.BitmapFormat
import javax.inject.Inject


class WSQImageDecoder @Inject constructor (private val bitmapConverter: BitmapConverter) {

    /**
     * Decode WSQ images
     *
     * @param imageBytes The WSQ encoded image
     *
     * @return the row image bytes array
     */
    fun decode(imageBytes: RawUnprocessedImage): FingerprintRawImage {

        if (!imageBytes.isValidFormat()) {
            throw BioSdkException.ImageDecodingException()
        }

        val decodingResult = WSQDecoder.decode(imageBytes.imageData)
            ?:throw BioSdkException.ImageDecodingException()
        val decodedBitmap = decodingResult.bitmap

        // WSQ decoder produces rgp images, NEC only process gray scale raw images.

        val rawImageBytes =
            bitmapConverter.convert(decodingResult.bitmap, BitmapFormat.BITMAP_8_BIT_COLOR)

        val decodedImage = FingerprintRawImage(
            imageBytes.un20SerialNumber,
            rawImageBytes,
            decodedBitmap.width,
            decodedBitmap.height,
            imageBytes.brightness

        )
        decodingResult.bitmap.recycle() // Recycle original bitmap to avoid OutOfMemoryException
        return decodedImage
    }

}
