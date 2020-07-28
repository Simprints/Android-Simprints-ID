package com.simprints.id.data.db.event.remote.models.callout

import androidx.annotation.Keep

@Keep
class ApiConfirmationCallout(val selectedGuid: String,
                             val sessionId: String): ApiCallout(ApiCalloutType.CONFIRMATION)
