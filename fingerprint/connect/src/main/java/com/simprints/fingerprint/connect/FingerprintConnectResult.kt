package com.simprints.fingerprint.connect

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult

@Keep
data class FingerprintConnectResult(
    val isSuccess: Boolean,
) : StepResult
