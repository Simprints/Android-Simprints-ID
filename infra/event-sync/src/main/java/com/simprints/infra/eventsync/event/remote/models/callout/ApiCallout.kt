package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep

@Keep
internal abstract class ApiCallout(
    val type: ApiCalloutType,
)
