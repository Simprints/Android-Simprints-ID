package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep
import java.util.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event as CoreEvent


@Keep
abstract class Event(var type: EventType,
                     open val starTime: Long,
                     open val endTime: Long? = null,
                     val id: String = UUID.randomUUID().toString())


