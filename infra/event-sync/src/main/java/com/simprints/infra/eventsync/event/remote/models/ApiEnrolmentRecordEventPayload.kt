package com.simprints.infra.eventsync.event.remote.models

import com.simprints.infra.eventsync.event.remote.ApiEnrolmentRecordPayloadType
import kotlinx.serialization.Serializable

@Serializable
internal sealed class ApiEnrolmentRecordEventPayload {
    abstract val type: ApiEnrolmentRecordPayloadType
}
