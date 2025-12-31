package com.simprints.infra.events.event.cosync

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class CoSyncEnrolmentRecordEvents(
    val events: List<EnrolmentRecordEvent>,
)
