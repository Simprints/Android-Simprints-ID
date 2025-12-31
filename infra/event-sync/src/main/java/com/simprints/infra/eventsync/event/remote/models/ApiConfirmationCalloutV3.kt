package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EventType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(EventType.CALLOUT_CONFIRMATION_V3_KEY)
internal data class ApiConfirmationCalloutV3(
    val selectedGuid: String,
    val sessionId: String,
    val metadata: String?,
    override val type: ApiCalloutType = ApiCalloutType.Confirmation,
) : ApiCallout() 
