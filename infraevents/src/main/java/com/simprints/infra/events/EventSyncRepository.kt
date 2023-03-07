package com.simprints.infra.events

import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.events.events_sync.down.domain.RemoteEventQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow

interface EventSyncRepository {

    suspend fun countEventsToUpload(projectId: String, type: EventType?): Flow<Int>

    fun uploadEvents(
        projectId: String,
        canSyncAllDataToSimprints: Boolean,
        canSyncBiometricDataToSimprints: Boolean,
        canSyncAnalyticsDataToSimprints: Boolean
    ): Flow<Int>

    suspend fun countEventsToDownload(query: RemoteEventQuery): List<EventCount>

    suspend fun downloadEvents(
            scope: CoroutineScope,
            query: RemoteEventQuery
    ): ReceiveChannel<EnrolmentRecordEvent>

}
