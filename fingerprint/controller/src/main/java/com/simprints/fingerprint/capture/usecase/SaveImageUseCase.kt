package com.simprints.fingerprint.capture.usecase

import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManager
import com.simprints.fingerprint.data.domain.images.deduceFileExtension
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class SaveImageUseCase @Inject constructor(
    private val imageManager: FingerprintImageManager,
) {

    suspend operator fun invoke(
        configuration: FingerprintConfiguration,
        captureEventId: String?,
        collectedFinger: CaptureState.Collected,
    ) = if (collectedFinger.scanResult.image != null && captureEventId != null) {
        imageManager.save(
            collectedFinger.scanResult.image,
            captureEventId,
            configuration.vero2!!.imageSavingStrategy.deduceFileExtension()
        )
    } else if (collectedFinger.scanResult.image != null && captureEventId == null) {
        Simber.e(FingerprintUnexpectedException("Could not save fingerprint image because of null capture ID"))
        null
    } else null
}
