package com.simprints.infra.eventsync

import androidx.lifecycle.LiveData
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface EventSyncManager {

    fun getPeriodicWorkTags(): List<String>
    fun getOneTimeWorkTags(): List<String>
    fun getAllWorkerTag(): String

    suspend fun getLastSyncTime(): Date?
    fun getLastSyncState(): LiveData<EventSyncState>

    suspend fun countEventsToUpload(type: EventType?): Flow<Int>
    suspend fun countEventsToDownload(): DownSyncCounts

    suspend fun downSyncSubject(projectId: String, subjectId: String)

    suspend fun deleteModules(unselectedModules: List<String>)
    suspend fun deleteSyncInfo()
    suspend fun resetDownSyncInfo()
}
