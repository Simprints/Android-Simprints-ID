package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep

@Keep
enum class BiometricDataSource {
    SIMPRINTS,
    COMMCARE;

    companion object {
        fun fromString(value: String) =
            when (value.uppercase()) {
                "COMMCARE" -> COMMCARE
                else -> SIMPRINTS
            }
    }
}
