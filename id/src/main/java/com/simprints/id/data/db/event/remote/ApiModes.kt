package com.simprints.id.data.db.event.remote

import com.simprints.id.data.db.event.remote.ApiModes.FACE
import com.simprints.id.data.db.event.remote.ApiModes.FINGERPRINT
import com.simprints.id.domain.modality.Modes
import io.realm.internal.Keep

@Keep
enum class ApiModes {
    FINGERPRINT,
    FACE;
}

fun Modes.fromDomainToApi(): ApiModes =
    when (this) {
        Modes.FINGERPRINT -> FINGERPRINT
        Modes.FACE -> FACE
    }
