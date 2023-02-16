package com.simprints.eventsystem.event.domain.models.subject

import androidx.annotation.Keep

@Keep
sealed class EnrolmentRecordEvent(
    open val id: String,
    val type: EnrolmentRecordEventType
)
