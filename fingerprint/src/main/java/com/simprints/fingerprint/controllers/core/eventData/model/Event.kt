package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import java.util.*

@Keep
abstract class Event(var type: EventType,
                     val id: String = UUID.randomUUID().toString(),
                     open val starTime: Long? = null,
                     open val endTime: Long? = null)
