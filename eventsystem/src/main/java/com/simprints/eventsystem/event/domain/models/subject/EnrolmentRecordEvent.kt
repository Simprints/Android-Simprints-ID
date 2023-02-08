package com.simprints.eventsystem.event.domain.models.subject

import androidx.annotation.Keep

@Keep
abstract class EnrolmentRecordEvent(
    open val id: String,
    val type: EnrolmentRecordEventType
)
