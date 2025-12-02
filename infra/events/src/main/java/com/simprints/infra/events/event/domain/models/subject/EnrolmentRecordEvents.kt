package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep

@Keep
data class EnrolmentRecordEvents(
    val events: List<EnrolmentRecordEvent>,
)
