package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class BiometricDataSource {
    SIMPRINTS,
    COMMCARE,
    ;

    companion object {
        fun fromString(value: String) = when (value.uppercase()) {
            "COMMCARE" -> COMMCARE
            else -> SIMPRINTS
        }
    }
}
