package com.simprints.id.data.db.person.remote.models

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
