package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep

@Keep
internal enum class ApiConfidenceMatch {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
}
