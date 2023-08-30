package com.simprints.feature.clientapi.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent

@Keep
internal data class CoSyncEnrolmentRecordEvents(
    val events: List<EnrolmentRecordEvent>,
)
