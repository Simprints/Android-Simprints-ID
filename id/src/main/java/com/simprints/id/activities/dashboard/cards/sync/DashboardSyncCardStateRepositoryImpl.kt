package com.simprints.id.activities.dashboard.cards.sync

import androidx.lifecycle.MediatorLiveData
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.*
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope.SubjectModuleScope
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.models.SubjectsDownSyncSetting.EXTRA
import com.simprints.id.services.sync.events.master.models.SubjectsDownSyncSetting.ON
import com.simprints.id.services.sync.events.master.models.SubjectsSyncState
import com.simprints.id.services.sync.events.master.models.SubjectsSyncWorkerState
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.*

class DashboardSyncCardStateRepositoryImpl(val eventSyncManager: EventSyncManager,
                                           val deviceManager: DeviceManager,
                                           private val preferencesManager: PreferencesManager,
                                           private val downSyncScopeRepository: EventDownSyncScopeRepository,
                                           private val cacheSync: EventSyncCache,
                                           private val timeHelper: TimeHelper) : DashboardSyncCardStateRepository {

    override val syncCardStateLiveData = MediatorLiveData<DashboardSyncCardState>()

    private var syncStateLiveData = eventSyncManager.getLastSyncState()
    private var isConnectedLiveData = deviceManager.isConnectedLiveData

    private var lastTimeSyncRun: Date? = null

    private val lastTimeSyncSucceed
        get() = cacheSync.readLastSuccessfulSyncTime()

    private var isUIBoundToSources = false

    private fun emitNewCardState(isConnected: Boolean,
                                 isModuleSelectionRequired: Boolean,
                                 syncState: SubjectsSyncState?) {

        val syncRunningAndInfoNotReadyYet = syncState == null && syncCardStateLiveData.value is SyncConnecting
        val syncNoRunningAndInfoNotReadyYet = syncState == null && syncCardStateLiveData.value !is SyncConnecting

        when {
            isModuleSelectionRequired -> SyncNoModules(lastTimeSyncSucceed)
            !isConnected -> SyncOffline(lastTimeSyncSucceed)
            syncRunningAndInfoNotReadyYet -> SyncConnecting(lastTimeSyncSucceed, 0, null)
            syncNoRunningAndInfoNotReadyYet -> SyncDefault(lastTimeSyncSucceed)
            syncState == null -> SyncDefault(null) //Useless after the 2 above - just to satisfy nullability in the else
            else -> processRecentSyncState(syncState)
        }.let { newState ->

            if (syncState != null && isSyncRunning(syncState.downSyncWorkersInfo + syncState.upSyncWorkersInfo)) {
                lastTimeSyncRun = Date()
            }

            updateDashboardCardState(newState)
        }
    }

    private fun processRecentSyncState(syncState: SubjectsSyncState): DashboardSyncCardState {

        val downSyncStates = syncState.downSyncWorkersInfo
        val upSyncStates = syncState.upSyncWorkersInfo
        val allSyncStates = downSyncStates + upSyncStates
        return when {
            isThereNotSyncHistory(allSyncStates) -> SyncDefault(lastTimeSyncSucceed)
            isSyncCompleted(allSyncStates) -> SyncComplete(lastTimeSyncSucceed)
            isSyncProcess(allSyncStates) -> SyncProgress(lastTimeSyncSucceed, syncState.progress, syncState.total)
            isSyncConnecting(allSyncStates) -> SyncConnecting(lastTimeSyncSucceed, syncState.progress, syncState.total)
            isSyncFailedBecauseCloudIntegration(allSyncStates) -> SyncFailed(lastTimeSyncSucceed)
            isSyncFailed(allSyncStates) -> SyncTryAgain(lastTimeSyncSucceed)
            else -> SyncProgress(lastTimeSyncSucceed, syncState.progress, syncState.total)
        }
    }

    override suspend fun syncIfRequired() {
        if (syncCardStateLiveData.value == null) {
            updateDashboardCardState(SyncConnecting(lastTimeSyncSucceed, 0, null))
        }

        var delayBeforeObserve = 0L
        if (shouldForceOneTimeSync()) {
            Timber.tag(SYNC_LOG_TAG).d("Re-launching one time sync")
            eventSyncManager.sync()
            delayBeforeObserve = 6000
        }

        // Only on the first launch, if sync gets triggered,
        // the UI binding is delayed, so only the new states are shown
        // instead of already terminated syncs
        if (!isUIBoundToSources) {
            isUIBoundToSources = true
            delay(delayBeforeObserve)
            bindUIToSync()
        }
    }

    private fun shouldForceOneTimeSync(): Boolean {
        val isRunning = syncStateLiveData.value?.let { isSyncRunning(it.downSyncWorkersInfo + it.upSyncWorkersInfo) } ?: false
        val lastUpdate = lastTimeSyncRun ?: lastTimeSyncSucceed

        return !isRunning && (lastUpdate == null || timeHelper.msBetweenNowAndTime(lastUpdate.time) > MAX_TIME_BEFORE_SYNC_AGAIN)
    }

    private suspend fun bindUIToSync() {
        val isModuleSelectionRequired = isModuleSelectionRequired()
        syncCardStateLiveData.addSource(isConnectedLiveData) { connectivity ->
            emitNewCardState(
                connectivity,
                isModuleSelectionRequired,
                syncStateLiveData.value)
        }

        syncCardStateLiveData.addSource(syncStateLiveData) { syncState ->
            emitNewCardState(
                isConnected(),
                isModuleSelectionRequired,
                syncState)
        }
    }

    private fun updateDashboardCardState(newState: DashboardSyncCardState) {
        Timber.tag(SYNC_LOG_TAG).d("new dashboard state: $newState")
        syncCardStateLiveData.value = newState
    }

    private fun isSyncFailedBecauseCloudIntegration(allSyncStates: List<SubjectsSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is SubjectsSyncWorkerState.Failed && it.state.failedBecauseCloudIntegration }

    private fun isThereNotSyncHistory(allSyncStates: List<SubjectsSyncState.SyncWorkerInfo>) = allSyncStates.isEmpty()

    private fun isSyncProcess(allSyncStates: List<SubjectsSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is SubjectsSyncWorkerState.Running }

    private fun isSyncFailed(allSyncStates: List<SubjectsSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is SubjectsSyncWorkerState.Failed || it.state is SubjectsSyncWorkerState.Blocked || it.state is SubjectsSyncWorkerState.Cancelled }

    private fun isSyncConnecting(allSyncStates: List<SubjectsSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is SubjectsSyncWorkerState.Enqueued }

    private fun isSyncCompleted(allSyncStates: List<SubjectsSyncState.SyncWorkerInfo>) =
        allSyncStates.all { it.state is SubjectsSyncWorkerState.Succeeded }


    private fun isSyncRunning(allSyncStates: List<SubjectsSyncState.SyncWorkerInfo>) =
        isSyncProcess(allSyncStates) || isSyncConnecting(allSyncStates)


    private suspend fun isModuleSelectionRequired() =
        isDownSyncAllowed() && isSelectedModulesEmpty() && isModuleSync()

    private fun isDownSyncAllowed() = with(preferencesManager) {
        subjectsDownSyncSetting == ON || subjectsDownSyncSetting == EXTRA
    }

    private fun isSelectedModulesEmpty() = preferencesManager.selectedModules.isEmpty()

    private suspend fun isModuleSync() = downSyncScopeRepository.getDownSyncScope() is SubjectModuleScope

    private fun isConnected() = isConnectedLiveData.value ?: true

    companion object {
        private const val ONE_MINUTE = 1000 * 60L
        const val MAX_TIME_BEFORE_SYNC_AGAIN = 5 * ONE_MINUTE
    }
}
