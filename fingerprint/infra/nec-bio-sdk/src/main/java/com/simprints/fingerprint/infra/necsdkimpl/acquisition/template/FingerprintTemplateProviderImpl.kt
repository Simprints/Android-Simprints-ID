package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.image.ProcessedImageCache
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprint.infra.scanner.v2.scanner.ScannerInfo
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class FingerprintTemplateProviderImpl @Inject constructor(
    private val fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory,
    private val calculateNecImageQualityUseCase: CalculateNecImageQualityUseCase,
    private val captureProcessedImageCache: ProcessedImageCache,
    private val extractNecTemplateUseCase: ExtractNecTemplateUseCase,
    private val processImage: ProcessRawImageUseCase,
    private val scannerInfo: ScannerInfo,
) : FingerprintTemplateProvider<FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata> {

    /**
     * Acquires a fingerprint template from the scanner.
     *
     * Steps involved:
     * 1. Acquire an unprocessed image from the scanner.
     * 2. Decode the WSQ unprocessed image using the SecuGen decoder.
     * 3. Apply the SecuGen algorithm to correct and upscale the raw image.
     * 4. Check the image quality using the NEC SDK.
     * 5. Convert the processed image into a template using the NEC SDK.
     * 6. Return the generated template and cache the image for future use.
     *
     **/
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun acquireFingerprintTemplate(
        settings: FingerprintTemplateAcquisitionSettings?
    ): TemplateResponse<FingerprintTemplateMetadata> {
        require(settings != null) { "Settings cannot be null" }
        val captureWrapper = fingerprintCaptureWrapperFactory.captureWrapper

        log("Acquiring unprocessed image")
        // Always require a new image from the scanner using the minimum resolution it will upsampled latter using secugen image correction
        val rawFingerprintScan = captureWrapper.acquireUnprocessedImage(
            Dpi(MIN_CAPTURE_DPI)
        ).rawUnprocessedImage
        // Store the recently captured image in the cache
        captureProcessedImageCache.recentlyCapturedImage = rawFingerprintScan.imageData
        // Store the serial number of the scanner for future use int the image upload
        scannerInfo.setUn20SerialNumber(rawFingerprintScan.un20SerialNumber.toHexString())
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
