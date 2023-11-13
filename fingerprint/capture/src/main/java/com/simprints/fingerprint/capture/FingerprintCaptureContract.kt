package com.simprints.fingerprint.capture

import com.simprints.core.domain.common.FlowType
import com.simprints.fingerprint.capture.screen.FingerprintCaptureFragmentArgs
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

object FingerprintCaptureContract {

    val DESTINATION = R.id.fingerprintCaptureFragment

    fun getArgs(
      flowType: FlowType,
      fingers: List<IFingerIdentifier>,
    ) = FingerprintCaptureFragmentArgs(FingerprintCaptureParams(flowType, fingers)).toBundle()

}
