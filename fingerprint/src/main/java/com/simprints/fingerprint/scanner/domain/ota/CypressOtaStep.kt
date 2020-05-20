package com.simprints.fingerprint.scanner.domain.ota

import com.simprints.fingerprint.scanner.tools.mapProgress

sealed class CypressOtaStep(val totalProgress: Float) {
    object Started : CypressOtaStep(0.00f)
    object OtaModeEntered : CypressOtaStep(0.05f)
    class TransferInProgress(otaProgress: Float) : CypressOtaStep(otaProgress.mapProgress(0.05f, 0.75f))
    object ReconnectingAfterTransfer : CypressOtaStep(0.75f)
    object ValidatingNewFirmwareVersion : CypressOtaStep(0.80f)
    object ReconnectingAfterValidating : CypressOtaStep(0.85f)
    object UpdatingUnifiedVersionInformation : CypressOtaStep(0.90f)
}
