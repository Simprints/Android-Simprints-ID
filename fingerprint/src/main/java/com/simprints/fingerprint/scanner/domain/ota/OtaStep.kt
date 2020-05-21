package com.simprints.fingerprint.scanner.domain.ota

import com.simprints.fingerprint.scanner.tools.mapProgress

sealed class OtaStep(val totalProgress: Float)

sealed class CypressOtaStep(totalProgress: Float) : OtaStep(totalProgress) {
    object EnteringOtaMode : CypressOtaStep(0.00f)
    object CommencingTransfer : CypressOtaStep(0.05f)
    data class TransferInProgress(val otaProgress: Float) : CypressOtaStep(otaProgress.mapProgress(0.05f, 0.85f))
    object ReconnectingAfterTransfer : CypressOtaStep(0.85f)
    object ValidatingNewFirmwareVersion : CypressOtaStep(0.90f)
    object UpdatingUnifiedVersionInformation : CypressOtaStep(0.95f)
}

sealed class StmOtaStep(totalProgress: Float) : OtaStep(totalProgress) {
    object EnteringOtaModeFirstTime : StmOtaStep(0.00f)
    object ReconnectingAfterEnteringOtaMode : StmOtaStep(0.05f)
    object EnteringOtaModeSecondTime : StmOtaStep(0.08f)
    object CommencingTransfer : StmOtaStep(0.10f)
    data class TransferInProgress(val otaProgress: Float) : StmOtaStep(otaProgress.mapProgress(0.10f, 0.70f))
    object ReconnectingAfterTransfer : StmOtaStep(0.70f)
    object EnteringMainMode : StmOtaStep(0.78f)
    object ValidatingNewFirmwareVersion : StmOtaStep(0.80f)
    object ReconnectingAfterValidating : StmOtaStep(0.85f)
    object UpdatingUnifiedVersionInformation : StmOtaStep(0.95f)
}

sealed class Un20OtaStep(totalProgress: Float) : OtaStep(totalProgress) {
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
