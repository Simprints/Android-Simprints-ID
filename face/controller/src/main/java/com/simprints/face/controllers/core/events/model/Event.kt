package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import java.util.*

@Keep
abstract class Event(
    var type: EventType,
    open val startTime: Long,
    open val endTime: Long = -1,
    val id: String = UUID.randomUUID().toString()
)
