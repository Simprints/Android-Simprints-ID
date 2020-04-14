package com.simprints.id.data.db.session.remote.events.callout

import androidx.annotation.Keep

@Keep
abstract class ApiCallout(val type: ApiCalloutType)
