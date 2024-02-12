package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.image.ProcessedImageCache
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.infra.logging.Simber
import com.simprints.sgimagecorrection.SecugenImageCorrection
import javax.inject.Inject

internal class FingerprintTemplateProviderImpl @Inject constructor(
    private val fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory,
    private val decodeWSQImageUseCase: DecodeWSQImageUseCase,
    private val secugenImageCorrection: SecugenImageCorrection,
    private val calculateNecImageQualityUseCase: CalculateNecImageQualityUseCase,
    private val captureProcessedImageCache: ProcessedImageCache,
    private val extractNecTemplateUseCase: ExtractNecTemplateUseCase
) :
    FingerprintTemplateProvider<FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata> {

    private lateinit var imageDistortionConfiguration: ByteArray

    override suspend fun acquireFingerprintTemplate(settings: FingerprintTemplateAcquisitionSettings?): TemplateResponse<FingerprintTemplateMetadata> {
        require(settings != null) { "Settings cannot be null" }
        readImageDistortionConfiguration()

        // 1- Acquire unprocessed image from the scanner
        // 2- Use secugen image processing to convert it to wsq format
        // 3- Use wsq sdk to convert it to bitmap
        // 4- Use nec sdk to check image quality
        // 5- Use nec sdk to convert it to template
        // 6- Return the template and cache the image for later use

        val captureWrapper = fingerprintCaptureWrapperFactory.captureWrapper

        // Always require a new image from the scanner using the minimum resolution as we will
        // process it using secugen image correction
        log("Acquiring unprocessed image")
        val unprocessedImage = captureWrapper.acquireUnprocessedImage(
            Dpi(MIN_CAPTURE_DPI)
        ).rawUnprocessedImage
        captureProcessedImageCache.recentlyCapturedImage = unprocessedImage.imageData
        log("Unprocessed image acquired, processing it")
        val decodedImage = decodeWSQImageUseCase(unprocessedImage)
        log("Image decoded successfully ${decodedImage.resolution}")
        log("processing image using secugen image correction")
        val secugenProcessedImage = processImage(settings, decodedImage)
        log("quality checking image using nec sdk")
        val qualityScore = calculateNecImageQualityUseCase(secugenProcessedImage)
        log("quality score is $qualityScore the threshold is ${settings.qualityThreshold}")
        if (qualityScore < settings.qualityThreshold)
            throw BioSdkException.ImageQualityBelowThresholdException(qualityScore)
        log("extracting template using nec sdk")
        return extractNecTemplateUseCase(secugenProcessedImage, qualityScore)
    }

    private fun log(message: String) {
        Simber.tag("NEC_SDK").d(message)
    }

    private suspend fun readImageDistortionConfiguration() {
        // if imageDistortionConfiguration not initialized read it from the scanner
        if (!::imageDistortionConfiguration.isInitialized) {
            log("Reading image distortion configuration from the scanner")
            imageDistortionConfiguration =
                fingerprintCaptureWrapperFactory.captureWrapper.acquireImageDistortionMatrixConfiguration()
                    .configurationBytes
            //Todo save the configuration in the datastore for later use to avoid reading it from the scanner every time
        }
    }

    private fun processImage(
        settings: FingerprintTemplateAcquisitionSettings,
        rawImage: FingerprintRawImage
    ): FingerprintImage {
        val scannerConfig = SecugenImageCorrection.ScannerConfig(
            imageDistortionConfiguration,
            settings.processingResolution?.value ?: DEFAULT_RESOLUTION,
            rawImage.un20SerialNumber,
            rawImage.brightness
        )
        val processedImage =
            secugenImageCorrection.processRawImage(rawImage.imageBytes, scannerConfig)
        return FingerprintImage(
            processedImage.imageBytes,
            processedImage.width,
            processedImage.height,
            processedImage.resolution
        )
    }


    companion object {
        private const val MIN_CAPTURE_DPI = 500.toShort()
    }

}

const val NEC_TEMPLATE_FORMAT = "NEC_1"
