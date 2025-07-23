package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep

@Keep
internal data class ApiConfirmationCalloutV2(
    val selectedGuid: String,
    val sessionId: String,
    val metadata: String?,
) : ApiCallout(ApiCalloutType.Confirmation)
