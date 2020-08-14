package com.simprints.id.data.db.event.remote.events.callout

import androidx.annotation.Keep

@Keep
class ApiConfirmationCallout(val selectedGuid: String,
                             val sessionId: String): ApiCallout(ApiCalloutType.CONFIRMATION)
