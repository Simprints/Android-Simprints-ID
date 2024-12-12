package com.simprints.fingerprint.capture

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.capture.screen.FingerprintCaptureFragmentArgs
import com.simprints.infra.config.store.models.FingerprintConfiguration

object FingerprintCaptureContract {
    val DESTINATION = R.id.fingerprintCaptureFragment

    fun getArgs(
        flowType: FlowType,
        fingers: List<IFingerIdentifier>,
        fingerprintSDK: FingerprintConfiguration.BioSdk,
    ) = FingerprintCaptureFragmentArgs(FingerprintCaptureParams(flowType, fingers, fingerprintSDK)).toBundle()
}
