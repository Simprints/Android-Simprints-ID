package com.simprints.infra.eventsync

import androidx.lifecycle.LiveData
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import kotlinx.coroutines.flow.Flow
import java.util.*

interface EventSyncManager {

    suspend fun getLastSyncTime(): Date?
    fun getLastSyncState(): LiveData<EventSyncState>

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()

    suspend fun countEventsToUpload(projectId: String, type: EventType?): Flow<Int>
    suspend fun countEventsToDownload(): DownSyncCounts

    suspend fun downSyncSubject(projectId: String, subjectId: String)

    suspend fun deleteModules(unselectedModules: List<String>)
    suspend fun deleteSyncInfo()
    suspend fun resetDownSyncInfo()
}
