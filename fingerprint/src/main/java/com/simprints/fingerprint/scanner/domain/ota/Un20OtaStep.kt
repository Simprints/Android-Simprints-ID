package com.simprints.fingerprint.scanner.domain.ota

import com.simprints.fingerprint.scanner.tools.mapProgress

sealed class Un20OtaStep(val totalProgress: Float) {
    object EnteringMainMode : Un20OtaStep(0.00f)
    object TurningOnUn20BeforeTransfer : Un20OtaStep(0.05f)
    object CommencingTransfer : Un20OtaStep(0.15f)
    data class TransferInProgress(val otaProgress: Float) : Un20OtaStep(otaProgress.mapProgress(0.15f, 0.60f))
    object TurningOffUn20AfterTransfer : Un20OtaStep(0.65f)
    object TurningOnUn20AfterTransfer : Un20OtaStep(0.70f)
    object ValidatingNewFirmwareVersion : Un20OtaStep(0.80f)
    object ReconnectingAfterValidating : Un20OtaStep(0.85f)
    object UpdatingUnifiedVersionInformation : Un20OtaStep(0.95f)
}
