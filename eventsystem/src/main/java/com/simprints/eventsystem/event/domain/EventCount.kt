package com.simprints.eventsystem.event.domain

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordEventType

@Keep
data class EventCount(val type: EnrolmentRecordEventType, val count: Int)
