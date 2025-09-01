package com.simprints.fingerprint.capture

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.ModalitySdkType

object FingerprintCaptureContract {
    val DESTINATION = R.id.fingerprintCaptureFragment

    fun getParams(
        flowType: FlowType,
        fingerprintSDK: ModalitySdkType,
    ) = FingerprintCaptureParams(flowType, fingerprintSDK)
}
