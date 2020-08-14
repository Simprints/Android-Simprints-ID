package com.simprints.id.data.db.event.remote.events.callout

import androidx.annotation.Keep

@Keep
abstract class ApiCallout(val type: ApiCalloutType)
