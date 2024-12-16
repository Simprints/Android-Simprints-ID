package com.simprints.infra.eventsync.status.down.domain

import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import kotlinx.coroutines.channels.ReceiveChannel

data class EventDownSyncResult(
    val totalCount: Int?,
    val status: Int,
    val eventStream: ReceiveChannel<EnrolmentRecordEvent>,
)
