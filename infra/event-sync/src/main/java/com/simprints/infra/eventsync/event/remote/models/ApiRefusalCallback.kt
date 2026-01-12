package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EventType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(EventType.CALLBACK_REFUSAL_KEY)
internal data class ApiRefusalCallback(
    val reason: String,
    val extra: String,
    override val type: ApiCallbackType = ApiCallbackType.Refusal,
) : ApiCallback()
