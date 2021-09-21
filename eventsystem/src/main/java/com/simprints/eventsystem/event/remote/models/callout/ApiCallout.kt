package com.simprints.eventsystem.event.remote.models.callout

import androidx.annotation.Keep

@Keep
abstract class ApiCallout(val type: ApiCalloutType)
