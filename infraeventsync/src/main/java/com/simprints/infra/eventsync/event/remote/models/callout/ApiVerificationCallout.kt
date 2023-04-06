package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep

@Keep
internal data class ApiVerificationCallout(
    val projectId: String,
    val userId: String,
    val moduleId: String,
    val metadata: String,
    val verifyGuid: String,
) : ApiCallout(ApiCalloutType.Verification)
