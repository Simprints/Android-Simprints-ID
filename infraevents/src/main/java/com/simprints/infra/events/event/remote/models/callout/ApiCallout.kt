package com.simprints.infra.events.remote.models.callout

import androidx.annotation.Keep

@Keep
abstract class ApiCallout(val type: ApiCalloutType)
