package com.simprints.feature.alert

import androidx.annotation.Keep
import com.simprints.core.domain.response.AppErrorReason
import java.io.Serializable

@Keep
data class AlertResult(
    val buttonKey: String,
    val appErrorReason: AppErrorReason? = null,
) : Serializable
