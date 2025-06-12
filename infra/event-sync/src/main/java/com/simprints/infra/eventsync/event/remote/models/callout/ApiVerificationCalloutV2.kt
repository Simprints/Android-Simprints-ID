package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep

@Keep
internal data class ApiVerificationCalloutV2(
    val projectId: String,
    val userId: String,
    val moduleId: String,
    val metadata: String,
    val verifyGuid: String,
) : ApiCallout(ApiCalloutType.Verification)
