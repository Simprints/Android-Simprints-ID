package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.MaxCaptureAttempts
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiMaxCaptureAttempts(
    val noFingerDetected: Int,
) {
    fun toDomain(): MaxCaptureAttempts = MaxCaptureAttempts(
        noFingerDetected = noFingerDetected,
    )
}
