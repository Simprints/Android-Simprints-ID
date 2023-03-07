package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep

@Keep
abstract class ApiCallout(val type: ApiCalloutType)
