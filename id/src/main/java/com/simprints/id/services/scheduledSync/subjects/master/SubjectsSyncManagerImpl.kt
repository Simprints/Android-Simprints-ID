package com.simprints.id.services.scheduledSync.subjects.master

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.up.SubjectsUpSyncScopeRepository
import com.simprints.id.services.scheduledSync.subjects.common.*
import com.simprints.id.services.scheduledSync.subjects.master.internal.SubjectsSyncCache
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncState
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsSyncMasterWorker
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULERS
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SubjectsSyncManagerImpl(private val ctx: Context,
                              private val subjectsSyncStateProcessor: SubjectsSyncStateProcessor,
                              private val subjectsUpSyncScopeRepository: SubjectsUpSyncScopeRepository,
                              private val subjectsDownSyncScopeRepository: SubjectsDownSyncScopeRepository,
                              private val subjectsSyncCache: SubjectsSyncCache) : SubjectsSyncManager {

    companion object {
        const val SYNC_REPEAT_INTERVAL = 60L
        val SYNC_REPEAT_UNIT = TimeUnit.MINUTES
    }

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    override fun getLastSyncState(): LiveData<SubjectsSyncState> =
        subjectsSyncStateProcessor.getLastSyncState()

    override fun hasSyncEverRunBefore(): Boolean =
        wm.getAllSubjectsSyncWorkersInfo().get().size > 0

    override fun sync() {
        Timber.tag(SYNC_LOG_TAG).d("Sync one time people master worker")
        wm.beginUniqueWork(
            MASTER_SYNC_SCHEDULER_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            buildOneTimeRequest()
        ).enqueue()
    }

    override fun scheduleSync() {
        Timber.tag(SYNC_LOG_TAG).d("Sync periodic people master worker")
        wm.enqueueUniquePeriodicWork(
            MASTER_SYNC_SCHEDULER_PERIODIC_TIME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildPeriodicRequest())

    }

    override fun cancelScheduledSync() {
        wm.cancelAllWorkByTag(MASTER_SYNC_SCHEDULERS)
    }

    override fun stop() {
        wm.cancelAllSubjectsSyncWorkers()
    }

    private fun cleanScheduledHistory() {
        wm.pruneWork()
    }

    private fun buildOneTimeRequest(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(SubjectsSyncMasterWorker::class.java)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTagForSyncMasterWorkers()
            .addTagForOneTimeSyncMasterWorker()
            .addTagForScheduledAtNow()
            .build() as OneTimeWorkRequest

    private fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequest.Builder(SubjectsSyncMasterWorker::class.java, SYNC_REPEAT_INTERVAL, SYNC_REPEAT_UNIT)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTagForSyncMasterWorkers()
            .addTagForBackgroundSyncMasterWorker()
            .addTagForScheduledAtNow()
            .build() as PeriodicWorkRequest

    private fun getDownSyncMasterWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    override suspend fun deleteSyncInfo() {
        subjectsUpSyncScopeRepository.deleteAll()
        subjectsDownSyncScopeRepository.deleteAll()
        subjectsSyncCache.clearProgresses()
        subjectsSyncCache.storeLastSuccessfulSyncTime(null)
        cleanScheduledHistory()
    }
}
