package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.secugen.WSQConverter
import com.simprints.core.DispatcherBG
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.RawUnprocessedImage
import com.simprints.sgimagecorrection.SecugenImageCorrection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class ProcessRawImageUseCase @Inject constructor(
    private val secugenImageCorrection: SecugenImageCorrection,
    private val acquireImageDistortionConfigurationUseCase: AcquireImageDistortionConfigurationUseCase,
    private val wsqConverter: WSQConverter,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        settings: FingerprintTemplateAcquisitionSettings,
        rawImage: RawUnprocessedImage,
        un20SerialNumber: ByteArray,
        brightness: Byte
    ): FingerprintImage = withContext(dispatcher) {
        val decodedImage = wsqConverter.fromWSQToRaw(rawImage.imageData)
        val scannerConfig = SecugenImageCorrection.ScannerConfig(
            acquireImageDistortionConfigurationUseCase(),
            settings.processingResolution?.value ?: DEFAULT_RESOLUTION,
            un20SerialNumber,
            brightness
        )
        val processedImage =
            secugenImageCorrection.processRawImage(decodedImage.bytes, scannerConfig)
        FingerprintImage(
            processedImage.imageBytes,
            processedImage.width,
            processedImage.height,
            processedImage.resolution
        )
    }
    companion object {
        private const val DEFAULT_RESOLUTION: Short = 500
    }
}
