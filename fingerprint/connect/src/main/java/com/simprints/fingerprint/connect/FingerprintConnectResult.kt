package com.simprints.fingerprint.connect

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("FingerprintConnectResult")
data class FingerprintConnectResult(
    val isSuccess: Boolean,
) : StepResult
