package com.simprints.feature.setup

import androidx.annotation.Keep
import com.simprints.core.domain.response.AppErrorReason
import java.io.Serializable

@Keep
data class SetupResult(
    val isSuccess: Boolean,
    val error: AppErrorReason? = null,
) : Serializable
