package com.simprints.infra.events.event.domain

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType

@Keep
data class EventCount(val type: EnrolmentRecordEventType, val count: Int)
