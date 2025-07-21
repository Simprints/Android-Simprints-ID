package com.simprints.fingerprint.connect

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.FingerprintConfiguration

@Keep
data class FingerprintConnectParams(
    val fingerprintSDK: FingerprintConfiguration.BioSdk,
) : StepParams
