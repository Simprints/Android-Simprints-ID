package com.simprints.infra.eventsync.status.down.domain

import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import kotlinx.coroutines.flow.Flow

data class CommCareEventSyncResult(
    val totalCount: Int?,
    val eventFlow: Flow<EnrolmentRecordEvent>,
)
