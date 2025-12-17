package com.simprints.fingerprint.capture

import androidx.annotation.Keep
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.FingerprintConfiguration

@Keep
data class FingerprintCaptureParams(
    val flowType: FlowType,
    val fingerprintsToCapture: List<TemplateIdentifier>,
    val fingerprintSDK: FingerprintConfiguration.BioSdk,
) : StepParams
