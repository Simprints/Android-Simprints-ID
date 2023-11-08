package com.simprints.fingerprint.capture

import com.simprints.core.domain.common.FlowProvider
import com.simprints.fingerprint.capture.screen.FingerprintCaptureFragmentArgs
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

object FingerprintCaptureContract {

    val DESTINATION = R.id.fingerprintCaptureFragment

    const val RESULT = "fingerprint_capture_result"

    fun getArgs(
        flowType: FlowProvider.FlowType,
        fingers: List<IFingerIdentifier>,
    ) = FingerprintCaptureFragmentArgs(FingerprintCaptureParams(flowType, fingers)).toBundle()

}