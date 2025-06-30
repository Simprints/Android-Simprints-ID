package com.simprints.infra.eventsync.event.remote.models.callout

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiEnrolmentLastBiometricsCalloutV2(
    val projectId: String,
    val userId: String,
    val moduleId: String,
    val metadata: String?,
    val sessionId: String,
) : ApiCallout(ApiCalloutType.EnrolmentLastBiometrics)
