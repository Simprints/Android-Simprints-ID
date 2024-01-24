package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.gemalto.wsq.WSQDecoder
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.RawUnprocessedImage
import com.ygoular.bitmapconverter.BitmapConverter
import com.ygoular.bitmapconverter.BitmapFormat
import javax.inject.Inject


class DecodeWSQImageUseCase @Inject constructor (private val bitmapConverter: BitmapConverter) {

    /**
     * Decode WSQ images
     *
     * @param imageBytes The WSQ encoded image
     *
     * @return the decoded raw image bytes array
     */
    operator fun invoke(imageBytes: RawUnprocessedImage): FingerprintRawImage {
        if (!imageBytes.isValidFormat()) {
            throw BioSdkException.ImageDecodingException()
       }
        val decodingResult = WSQDecoder.decode(imageBytes.imageData)
            ?:throw BioSdkException.ImageDecodingException()
        val decodedBitmap = decodingResult.bitmap

        // WSQ decoder produces rgp images, NEC only process gray scale raw images.
        // So we convert the decoded bitmap to gray scale.
        val rawImageBytes =
            bitmapConverter.convert(decodingResult.bitmap, BitmapFormat.BITMAP_8_BIT_COLOR)

        val decodedImage = FingerprintRawImage(
            imageBytes.un20SerialNumber,
            rawImageBytes,
            decodedBitmap.width,
            decodedBitmap.height,
            imageBytes.brightness
        )
        // Recycle the bitmap to free memory and avoid OutOfMemoryException
        decodingResult.bitmap.recycle()
        return decodedImage
    }
}
