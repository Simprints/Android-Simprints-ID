package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.EventCount

@Keep
data class ApiEventCount(val type: ApiEventPayloadType, val count: Int)

fun ApiEventCount.fromApiToDomain() = EventCount(type.fromApiToDomain(), count)
