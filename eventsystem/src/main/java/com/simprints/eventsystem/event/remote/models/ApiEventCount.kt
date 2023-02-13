package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.EventCount
import com.simprints.eventsystem.event.remote.models.subject.ApiEnrolmentRecordPayloadType
import com.simprints.eventsystem.event.remote.models.subject.fromApiToDomain

@Keep
data class ApiEventCount(val type: ApiEnrolmentRecordPayloadType, val count: Int)

fun ApiEventCount.fromApiToDomain() = EventCount(type.fromApiToDomain(), count)
