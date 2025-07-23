package com.simprints.feature.alert

import androidx.annotation.Keep
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.step.StepResult

@Keep
data class AlertResult(
    val buttonKey: String,
    val appErrorReason: AppErrorReason? = null,
) : StepResult
