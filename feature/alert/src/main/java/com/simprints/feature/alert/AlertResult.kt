package com.simprints.feature.alert

import androidx.annotation.Keep
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("AlertResult")
data class AlertResult(
    val buttonKey: String,
    val appErrorReason: AppErrorReason? = null,
) : StepResult
