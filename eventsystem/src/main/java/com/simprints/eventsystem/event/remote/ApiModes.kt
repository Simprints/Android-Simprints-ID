package com.simprints.eventsystem.event.remote

import androidx.annotation.Keep
import com.simprints.core.domain.modality.Modes


@Keep
internal enum class ApiModes {
    FINGERPRINT,
    FACE;
}

internal fun Modes.fromDomainToApi(): ApiModes =
    when (this) {
        Modes.FINGERPRINT -> ApiModes.FINGERPRINT
        Modes.FACE -> ApiModes.FACE
    }
