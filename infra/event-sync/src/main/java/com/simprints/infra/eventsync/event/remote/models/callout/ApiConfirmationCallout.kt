package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep

@Keep
internal data class ApiConfirmationCallout(
    val selectedGuid: String,
    val sessionId: String,
) : ApiCallout(ApiCalloutType.Confirmation)
