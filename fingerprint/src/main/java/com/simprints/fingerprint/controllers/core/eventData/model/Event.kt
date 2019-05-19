package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import java.util.*

@Keep
abstract class Event(var type: EventType,
                     open val starTime: Long,
                     open val endTime: Long = -1,
                     val id: String = UUID.randomUUID().toString())
