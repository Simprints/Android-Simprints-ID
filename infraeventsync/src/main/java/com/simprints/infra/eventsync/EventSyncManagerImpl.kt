package com.simprints.infra.eventsync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.RemoteEventQuery
import com.simprints.infra.eventsync.status.down.domain.fromDomainToApi
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.sync.EventSyncStateProcessor
import com.simprints.infra.eventsync.sync.common.*
import com.simprints.infra.eventsync.sync.down.EventDownSyncHelper
import com.simprints.infra.eventsync.sync.master.EventSyncMasterWorker
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class EventSyncManagerImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val eventSyncStateProcessor: EventSyncStateProcessor,
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
    private val eventRepository: EventRepository,
    private val upSyncScopeRepo: EventUpSyncScopeRepository,
    private val eventSyncCache: EventSyncCache,
    private val downSyncHelper: EventDownSyncHelper,
    private val eventRemoteDataSource: EventRemoteDataSource,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : EventSyncManager {

    companion object {
        private const val SYNC_REPEAT_INTERVAL = BuildConfig.SYNC_PERIODIC_WORKER_INTERVAL_MINUTES
        val SYNC_REPEAT_UNIT = TimeUnit.MINUTES
    }

    private val wm = WorkManager.getInstance(ctx)

    override suspend fun getLastSyncTime(): Date? = eventSyncCache.readLastSuccessfulSyncTime()

    override fun getLastSyncState(): LiveData<EventSyncState> =
        eventSyncStateProcessor.getLastSyncState()

    override fun hasSyncEverRunBefore(): Boolean =
        wm.getAllSubjectsSyncWorkersInfo().get().size > 0

    override fun sync() {
        Simber.tag(SYNC_LOG_TAG).d("[SCHEDULER] One time events master worker")

        wm.beginUniqueWork(
            MASTER_SYNC_SCHEDULER_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            buildOneTimeRequest()
        ).enqueue()
    }

    override fun scheduleSync() {
        Simber.tag(SYNC_LOG_TAG).d("[SCHEDULER] Periodic events master worker")

        wm.enqueueUniquePeriodicWork(
            MASTER_SYNC_SCHEDULER_PERIODIC_TIME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildPeriodicRequest()
        )
    }

    private fun buildOneTimeRequest(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventSyncMasterWorker::class.java)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTagForSyncMasterWorkers()
            .addTagForOneTimeSyncMasterWorker()
            .addTagForScheduledAtNow()
            .build() as OneTimeWorkRequest

    private fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequest.Builder(
            EventSyncMasterWorker::class.java,
            SYNC_REPEAT_INTERVAL,
            SYNC_REPEAT_UNIT
        )
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTagForSyncMasterWorkers()
            .addTagForBackgroundSyncMasterWorker()
            .addTagForScheduledAtNow()
            .build() as PeriodicWorkRequest

    private fun getDownSyncMasterWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    override fun cancelScheduledSync() {
        wm.cancelAllWorkByTag(MASTER_SYNC_SCHEDULERS)
        stop()
    }

    override fun stop() {
        wm.cancelAllSubjectsSyncWorkers()
    }

    private fun cleanScheduledHistory() {
        wm.pruneWork()
    }

    override suspend fun countEventsToUpload(projectId: String, type: EventType?): Flow<Int> =
        eventRepository.observeEventCount(projectId, type)

    override suspend fun getDownSyncCounts(
        modes: List<Modes>,
        modules: List<String>,
        group: GROUP,
    ): DownSyncCounts {
        val downSyncScope = downSyncScopeRepository.getDownSyncScope(modes, modules, group)

        var creationsToDownload = 0
        var deletionsToDownload = 0

        downSyncScope.operations.forEach { syncOperation ->
            val counts = eventRemoteDataSource.count(syncOperation.queryEvent.fromDomainToApi())

            creationsToDownload += counts
                .firstOrNull { it.type == EnrolmentRecordEventType.EnrolmentRecordCreation }
                ?.count ?: 0
            deletionsToDownload += counts
                .firstOrNull { it.type == EnrolmentRecordEventType.EnrolmentRecordDeletion }
                ?.count ?: 0
        }

        return DownSyncCounts(creationsToDownload, deletionsToDownload)
    }

    override suspend fun downSync(
        projectId: String,
        subjectId: String,
        modes: List<Modes>,
    ): Unit = withContext(dispatcher) {
        val op = EventDownSyncOperation(RemoteEventQuery(
            projectId = projectId,
            subjectId = subjectId,
            modes = modes,
        ))
        downSyncHelper.downSync(this, op).toList()
    }

    override suspend fun deleteModules(unselectedModules: List<String>, modes: List<Modes>) {
        downSyncScopeRepository.deleteOperations(unselectedModules, modes)
    }

    override suspend fun deleteSyncInfo() {
        downSyncScopeRepository.deleteAll()
        upSyncScopeRepo.deleteAll()
        eventSyncCache.clearProgresses()
        eventSyncCache.storeLastSuccessfulSyncTime(null)
        cleanScheduledHistory()
    }

    override suspend fun resetDownSyncInfo() {
        downSyncScopeRepository.deleteAll()
    }

}
