package com.simprints.fingerprint.capture

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.infra.config.store.models.ModalitySdkType

object FingerprintCaptureContract {
    val DESTINATION = R.id.fingerprintCaptureFragment

    fun getParams(
        flowType: FlowType,
        fingers: List<TemplateIdentifier>,
        fingerprintSDK: ModalitySdkType,
    ) = FingerprintCaptureParams(flowType, fingers, fingerprintSDK)
}
