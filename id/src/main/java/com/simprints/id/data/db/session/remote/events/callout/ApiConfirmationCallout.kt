package com.simprints.id.data.db.session.remote.events.callout

import androidx.annotation.Keep

@Keep
class ApiConfirmationCallout(val selectedGuid: String,
                             val sessionId: String): ApiCallout(ApiCalloutType.CONFIRMATION)
