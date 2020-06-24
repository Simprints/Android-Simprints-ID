package com.simprints.id.data.db.event.remote.events.subject

import com.simprints.id.domain.modality.Modes
import io.realm.internal.Keep

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
