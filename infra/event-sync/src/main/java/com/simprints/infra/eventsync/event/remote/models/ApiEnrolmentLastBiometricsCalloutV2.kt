package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiEnrolmentLastBiometricsCalloutV2(
    val projectId: String,
    val userId: String,
    val moduleId: String,
    val metadata: String?,
    val sessionId: String,
    override val type: ApiCalloutType = ApiCalloutType.EnrolmentLastBiometrics,
) : ApiCallout()
