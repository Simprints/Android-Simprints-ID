package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.image.ProcessedImageCache
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class FingerprintTemplateProviderImpl @Inject constructor(
    private val fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory,
    private val calculateNecImageQualityUseCase: CalculateNecImageQualityUseCase,
    private val captureProcessedImageCache: ProcessedImageCache,
    private val extractNecTemplateUseCase: ExtractNecTemplateUseCase,
    private val processImage: ProcessRawImageUseCase,
) : FingerprintTemplateProvider<FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata> {

    override suspend fun acquireFingerprintTemplate(
        settings: FingerprintTemplateAcquisitionSettings?
    ): TemplateResponse<FingerprintTemplateMetadata> {
        require(settings != null) { "Settings cannot be null" }

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
        val rawFingerprintScan = captureWrapper.acquireUnprocessedImage(
            Dpi(MIN_CAPTURE_DPI)
        ).rawUnprocessedImage
        captureProcessedImageCache.recentlyCapturedImage = rawFingerprintScan.imageData
        log("processing image using secugen image correction")
        val secugenProcessedImage = processImage(
            settings,
            rawFingerprintScan,
            rawFingerprintScan.un20SerialNumber,
            rawFingerprintScan.brightness
        )
        log("quality checking image using nec sdk")
        val qualityScore = calculateNecImageQualityUseCase(secugenProcessedImage)
        log("quality score is $qualityScore the threshold is ${settings.qualityThreshold}")
        return if (qualityScore < settings.qualityThreshold && !settings.allowLowQualityExtraction)
        // if the quality score is less than the threshold return an empty template
            TemplateResponse(
                byteArrayOf(), FingerprintTemplateMetadata(
                    templateFormat = NEC_TEMPLATE_FORMAT, imageQualityScore = qualityScore
                )
            ) else {
            log("extracting template using nec sdk")
            extractNecTemplateUseCase(secugenProcessedImage, qualityScore)
        }
    }

    private fun log(message: String) {
        Simber.tag("NEC_SDK").d(message)
    }

    companion object {
        private const val MIN_CAPTURE_DPI = 500.toShort()
    }

}

const val NEC_TEMPLATE_FORMAT = "NEC_1_5"
