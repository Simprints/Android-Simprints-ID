package com.simprints.fingerprint.scanner.domain.ota

import com.simprints.fingerprint.scanner.tools.mapProgress

sealed class StmOtaStep(val totalProgress: Float) {
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
