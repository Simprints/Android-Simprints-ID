package com.simprints.fingerprint.capture

import androidx.annotation.Keep
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.ModalitySdkType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("FingerprintCaptureParams")
data class FingerprintCaptureParams(
    val flowType: FlowType,
    val fingerprintsToCapture: List<TemplateIdentifier>,
    val fingerprintSDK: ModalitySdkType,
) : StepParams
