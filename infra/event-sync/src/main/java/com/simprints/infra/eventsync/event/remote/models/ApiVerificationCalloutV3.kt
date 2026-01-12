package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EventType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(EventType.CALLOUT_VERIFICATION_V3_KEY)
internal data class ApiVerificationCalloutV3(
    val projectId: String,
    val userId: String,
    val moduleId: String,
    val metadata: String,
    val verifyGuid: String,
    val biometricDataSource: ApiBiometricDataSource,
    override val type: ApiCalloutType = ApiCalloutType.Verification,
) : ApiCallout()
