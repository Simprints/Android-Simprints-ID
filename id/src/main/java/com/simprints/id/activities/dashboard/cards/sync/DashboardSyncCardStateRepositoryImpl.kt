package com.simprints.id.activities.dashboard.cards.sync

import androidx.lifecycle.MediatorLiveData
import com.simprints.core.domain.modality.toMode
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope.SubjectModuleScope
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncComplete
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncConnecting
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncDefault
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncFailed
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncFailedBackendMaintenance
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncHasNoModules
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncOffline
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncProgress
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncTryAgain
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.EXTRA
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.ON
import com.simprints.id.services.sync.events.master.models.EventSyncState
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState
import com.simprints.id.tools.device.DeviceManager
import com.simprints.logging.Simber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.coroutines.coroutineContext

class DashboardSyncCardStateRepositoryImpl(
    val eventSyncManager: EventSyncManager,
    val deviceManager: DeviceManager,
    private val preferencesManager: IdPreferencesManager,
    private val downSyncScopeRepository: com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository,
    private val cacheSync: EventSyncCache,
    private val timeHelper: TimeHelper
) : DashboardSyncCardStateRepository {

    override val syncCardStateLiveData = MediatorLiveData<DashboardSyncCardState>()

    private var syncStateLiveData = eventSyncManager.getLastSyncState()
    private var isConnectedLiveData = deviceManager.isConnectedLiveData

    private var lastTimeSyncRun: Date? = null
    private var estimatedOutage: Long? = null

    private val lastTimeSyncSucceed
        get() = cacheSync.readLastSuccessfulSyncTime()

    private var isUIBoundToSources = false

    private fun emitNewCardState(
        isConnected: Boolean,
        isModuleSelectionRequired: Boolean,
        syncState: EventSyncState?
    ) {

        val syncRunningAndInfoNotReadyYet =
            syncState == null && syncCardStateLiveData.value is SyncConnecting
        val syncNotRunningAndInfoNotReadyYet =
            syncState == null && syncCardStateLiveData.value !is SyncConnecting

        when {
            isModuleSelectionRequired -> SyncHasNoModules(lastTimeSyncSucceed)
            !isConnected -> SyncOffline(lastTimeSyncSucceed)
            syncRunningAndInfoNotReadyYet -> SyncConnecting(lastTimeSyncSucceed, 0, null)
            syncNotRunningAndInfoNotReadyYet -> SyncDefault(lastTimeSyncSucceed)
            syncState == null -> SyncDefault(null) //Useless after the 2 above - just to satisfy nullability in the else
            else -> processRecentSyncState(syncState)
        }.let { newState ->

            if (syncState != null && isSyncRunning(syncState.downSyncWorkersInfo + syncState.upSyncWorkersInfo)) {
                lastTimeSyncRun = Date()
            }

            updateDashboardCardState(newState)
        }
    }

    private fun processRecentSyncState(syncState: EventSyncState): DashboardSyncCardState {

        val downSyncStates = syncState.downSyncWorkersInfo
        val upSyncStates = syncState.upSyncWorkersInfo
        val allSyncStates = downSyncStates + upSyncStates
        return when {
            isThereNotSyncHistory(allSyncStates) -> SyncDefault(lastTimeSyncSucceed)
            isSyncCompleted(allSyncStates) -> SyncComplete(lastTimeSyncSucceed)
            isSyncProcess(allSyncStates) -> SyncProgress(
                lastTimeSyncSucceed,
                syncState.progress,
                syncState.total
            )
            isSyncConnecting(allSyncStates) -> SyncConnecting(
                lastTimeSyncSucceed,
                syncState.progress,
                syncState.total
            )
            isSyncFailedBecauseCloudIntegration(allSyncStates) -> SyncFailed(lastTimeSyncSucceed)
            isSyncFailedBecauseBackendMaintenance(allSyncStates) -> SyncFailedBackendMaintenance(lastTimeSyncSucceed, estimatedOutage)
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
            Simber.tag(SYNC_LOG_TAG).d("[ACTIVITY]\n Re-launching one time sync")
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
        val isRunning =
            syncStateLiveData.value?.let { isSyncRunning(it.downSyncWorkersInfo + it.upSyncWorkersInfo) }
                ?: false
        val lastUpdate = lastTimeSyncRun ?: lastTimeSyncSucceed

        return !isRunning && (lastUpdate == null || timeHelper.msBetweenNowAndTime(lastUpdate.time) > MAX_TIME_BEFORE_SYNC_AGAIN)
    }

    private suspend fun bindUIToSync() {
        val scope = coroutineContext
        syncCardStateLiveData.addSource(isConnectedLiveData) { connectivity ->
            CoroutineScope(scope + SupervisorJob()).launch {
                emitNewCardState(
                    connectivity,
                    isModuleSelectionRequired(),
                    syncStateLiveData.value
                )
            }
        }

        syncCardStateLiveData.addSource(syncStateLiveData) { syncState ->
            CoroutineScope(scope + SupervisorJob()).launch {
                emitNewCardState(
                    isConnected(),
                    isModuleSelectionRequired(),
                    syncState
                )
            }
        }
    }

    private fun updateDashboardCardState(newState: DashboardSyncCardState) {
        Simber.tag(SYNC_LOG_TAG).d("[ACTIVITY]\n New dashboard state: $newState")
        syncCardStateLiveData.value = newState
    }

    private fun isSyncFailedBecauseCloudIntegration(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseCloudIntegration }

    private fun isSyncFailedBecauseBackendMaintenance(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseBackendMaintenance }

    private fun isThereNotSyncHistory(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.isEmpty()

    private fun isSyncProcess(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is EventSyncWorkerState.Running }

    private fun isSyncFailed(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is EventSyncWorkerState.Failed || it.state is EventSyncWorkerState.Blocked || it.state is EventSyncWorkerState.Cancelled }

    private fun isSyncConnecting(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is EventSyncWorkerState.Enqueued }

    private fun isSyncCompleted(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.all { it.state is EventSyncWorkerState.Succeeded }


    private fun isSyncRunning(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        isSyncProcess(allSyncStates) || isSyncConnecting(allSyncStates)


    private suspend fun isModuleSelectionRequired() =
        isDownSyncAllowed() && isSelectedModulesEmpty() && isModuleSync()

    private fun isDownSyncAllowed() = with(preferencesManager) {
        eventDownSyncSetting == ON || eventDownSyncSetting == EXTRA
    }

    private fun isSelectedModulesEmpty() = preferencesManager.selectedModules.isEmpty()

    private suspend fun isModuleSync() =
        downSyncScopeRepository.getDownSyncScope(
            preferencesManager.modalities.map { it.toMode() },
            preferencesManager.moduleIdOptions.toList(),
            preferencesManager.syncGroup
        ) is SubjectModuleScope

    private fun isConnected() = isConnectedLiveData.value ?: true

    companion object {
        private const val ONE_MINUTE = 1000 * 60L
        const val MAX_TIME_BEFORE_SYNC_AGAIN = 5 * ONE_MINUTE
    }
}
