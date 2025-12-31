package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EventType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(EventType.CALLBACK_ENROLMENT_KEY)
internal data class ApiEnrolmentCallback(
    val guid: String,
    override val type: ApiCallbackType = ApiCallbackType.Enrolment,
) : ApiCallback()
