package com.simprints.id.data.analytics.eventdata.models.remote.events.callout

import androidx.annotation.Keep

@Keep
class ApiConfirmationCallout(val selectedGuid: String,
                             val sessionId: String): ApiCallout(ApiCalloutType.CONFIRMATION)
