package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep

@Keep
internal data class ApiEvent(
    val id: String,
    val type: ApiEventPayloadType,
    val version: Int,
    val payload: ApiEventPayload,
    val tokenizedFields: List<String>,
)
