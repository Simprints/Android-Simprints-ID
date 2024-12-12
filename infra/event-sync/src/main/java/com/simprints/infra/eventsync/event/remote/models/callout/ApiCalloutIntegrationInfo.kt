package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep

@Keep
internal enum class ApiCalloutIntegrationInfo {
    ODK,
    STANDARD,
}
