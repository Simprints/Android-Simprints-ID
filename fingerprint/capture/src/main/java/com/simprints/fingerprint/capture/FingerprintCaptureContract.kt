package com.simprints.fingerprint.capture

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.infra.config.store.models.FingerprintConfiguration

object FingerprintCaptureContract {
    val DESTINATION = R.id.fingerprintCaptureFragment

    fun getParams(
        flowType: FlowType,
        fingers: List<SampleIdentifier>,
        fingerprintSDK: FingerprintConfiguration.BioSdk,
    ) = FingerprintCaptureParams(flowType, fingers, fingerprintSDK)
}
