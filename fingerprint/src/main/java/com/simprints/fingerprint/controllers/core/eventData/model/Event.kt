package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import java.util.*

/**
 * This class represents a fingerprint Event that occurs during a fingerprint capture.
 * @see [EventType] for different types of events.
 *
 * @property type  the type of event being captured
 * @property startTime  the time the event started, in MilliSeconds
 * @property endTime  the time the event ended, in MilliSeconds
 * @property id  a unique id created for the event
 */
@Keep
abstract class Event(var type: EventType,
                     open val startTime: Long,
                     open val endTime: Long = -1,
                     val id: String = UUID.randomUUID().toString())
