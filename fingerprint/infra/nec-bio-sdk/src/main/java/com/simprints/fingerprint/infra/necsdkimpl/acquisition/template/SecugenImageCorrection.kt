package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template


import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.sgimagecorrection.SecugenImageCorrectionException
import com.simprints.sgimagecorrection.SecugenWrapper
import javax.inject.Inject

/**
 * Secugen image correction
 * refer to [this](https://docs.google.com/document/d/1-s6En3OBdZJV7wJdSGTEhPBhfh6RPmrktc7dzHlXp1o/edit?pli=1#heading=h.vaa2e2xf6ifs) document for more details about secugen correction
 *
 */

class SecugenImageCorrection @Inject constructor(private val secugenWrapper: SecugenWrapper) {

    /**
     * Process raw image
     * This method takes a raw image and returns a processed image using secugen SDK correction algorithm
     */
    fun processRawImage(srcImage: ByteArray, scannerConfig: ScannerConfig) = try {
         getFullImage(srcImage, scannerConfig)
    } catch (exception: SecugenImageCorrectionException) {
        log("processing raw image failed")
        throw BioSdkException.ImageProcessingException(exception)
    }


    private fun getFullImage(
        srcImage: ByteArray,
        scannerConfig: ScannerConfig,
    ): FingerprintImage {
        initSgImage(scannerConfig)
        val imageDimensions = getDestinationDimensions()
        val destinationImageBytes = ByteArray(imageDimensions.first * imageDimensions.second)
        secugenWrapper.getImage(
            scannerConfig.serialNumber,
            scannerConfig.brightness.toInt(),
            srcImage,
            destinationImageBytes
        )
        secugenWrapper.destroy()
        return FingerprintImage(
            destinationImageBytes,
            imageDimensions.first,
            imageDimensions.second,
            scannerConfig.dpi.toInt()
        )
    }

    private fun initSgImage(scannerConfig: ScannerConfig) {
        secugenWrapper.apply {
            create()
            initData(scannerConfig.configData)
            setDPI(scannerConfig.dpi)
        }
    }


    private fun getDestinationDimensions(): Pair<Int, Int> {
        val destinationWidth = ShortArray(1)
        val destinationHeight = ShortArray(1)
        secugenWrapper.getImageSizeByDPI(destinationWidth, destinationHeight)
        return Pair(destinationWidth[0].toInt(), destinationHeight[0].toInt())
    }

    class ScannerConfig(
        val configData: ByteArray,
        val dpi: Short,
        val serialNumber: ByteArray,
        val brightness: Byte
    )

}
