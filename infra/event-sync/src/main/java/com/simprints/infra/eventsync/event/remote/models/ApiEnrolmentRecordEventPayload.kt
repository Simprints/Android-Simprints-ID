package com.simprints.infra.eventsync.event.remote.models

import com.simprints.infra.eventsync.event.remote.ApiEnrolmentRecordPayloadType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal sealed class ApiEnrolmentRecordEventPayload {
    abstract val type: ApiEnrolmentRecordPayloadType
}
