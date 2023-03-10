package com.simprints.infra.eventsync

import androidx.lifecycle.LiveData
import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import kotlinx.coroutines.flow.Flow
import java.util.*

interface EventSyncManager {

    suspend fun getLastSyncTime(): Date?
    fun getLastSyncState(): LiveData<EventSyncState>
    fun hasSyncEverRunBefore(): Boolean

    fun sync()
    fun stop()

    fun scheduleSync()
    fun cancelScheduledSync()

    suspend fun countEventsToUpload(projectId: String, type: EventType?): Flow<Int>

    suspend fun getDownSyncCounts(modes: List<Modes>, modules: List<String>, group: GROUP): DownSyncCounts
    suspend fun downSync(projectId: String, subjectId: String, modes: List<Modes>)

    suspend fun deleteModules(unselectedModules: List<String>, modes: List<Modes>)
    suspend fun deleteSyncInfo()
    suspend fun resetDownSyncInfo()
}
