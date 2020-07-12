package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.common.models.EventCount

@Keep
class ApiEventCount(val type: ApiEventPayloadType, val count: Int)

fun ApiEventCount.fromApiToDomain() = EventCount(type.fromApiToDomain(), count)
