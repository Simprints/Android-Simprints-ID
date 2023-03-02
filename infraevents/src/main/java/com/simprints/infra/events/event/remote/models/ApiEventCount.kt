package com.simprints.infra.events.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.remote.models.subject.ApiEnrolmentRecordPayloadType
import com.simprints.infra.events.remote.models.subject.fromApiToDomain

@Keep
data class ApiEventCount(val type: ApiEnrolmentRecordPayloadType, val count: Int)

fun ApiEventCount.fromApiToDomain() = EventCount(type.fromApiToDomain(), count)
