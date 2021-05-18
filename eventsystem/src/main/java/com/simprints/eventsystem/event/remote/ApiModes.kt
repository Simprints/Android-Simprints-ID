package com.simprints.eventsystem.event.remote

import androidx.annotation.Keep
import com.simprints.core.domain.modality.Modes


@Keep
enum class ApiModes {
    FINGERPRINT,
    FACE;
}

fun Modes.fromDomainToApi(): ApiModes =
    when (this) {
        Modes.FINGERPRINT -> ApiModes.FINGERPRINT
        Modes.FACE -> ApiModes.FACE
    }
