package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiEvent(
    val id: String,
    val type: ApiEventPayloadType,
    val version: Int,
    val payload: ApiEventPayload,
    val tokenizedFields: List<String>,
)
