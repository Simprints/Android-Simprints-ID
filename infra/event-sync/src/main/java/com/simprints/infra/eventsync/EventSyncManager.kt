package com.simprints.infra.eventsync

import androidx.lifecycle.LiveData
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import kotlinx.coroutines.flow.Flow

interface EventSyncManager {
    fun getPeriodicWorkTags(): List<String>

    fun getOneTimeWorkTags(): List<String>

    fun getAllWorkerTag(): String

    suspend fun getLastSyncTime(): Timestamp?

    fun getLastSyncState(useDefaultValue: Boolean = false): LiveData<EventSyncState>

    suspend fun countEventsToUpload(): Flow<Int>

    suspend fun countEventsToUpload(types: List<EventType>): Flow<Int>

    suspend fun countEventsToDownload(maxCacheAgeMillis: Long = 0): DownSyncCounts

    suspend fun downSyncSubject(
        projectId: String,
        subjectId: String,
    )

    suspend fun deleteModules(unselectedModules: List<String>)

    suspend fun deleteSyncInfo()

    suspend fun resetDownSyncInfo()
}
