package com.simprints.face.configuration

import androidx.annotation.Keep
import com.simprints.core.domain.response.AppErrorReason
import java.io.Serializable

@Keep
data class FaceConfigurationResult(
    val isSuccess: Boolean,
    val error: AppErrorReason? = null,
) : Serializable
