package com.simprints.feature.dashboard.main.sync

import androidx.lifecycle.*
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.events_sync.models.EventSyncState
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerState
import com.simprints.feature.dashboard.main.sync.DashboardSyncCardState.*
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.canSyncDataToSimprints
import com.simprints.infra.config.domain.models.isEventDownSyncAllowed
import com.simprints.infra.login.LoginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject

@HiltViewModel
internal class SyncViewModel @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val deviceManager: DeviceManager,
    private val configManager: ConfigManager,
    private val cacheSync: EventSyncCache,
    private val timeHelper: TimeHelper,
    private val loginManager: LoginManager,
    private val eventRepository: EventRepository,
) : ViewModel() {

    companion object {
        private const val ONE_MINUTE = 1000 * 60L
        private const val MAX_TIME_BEFORE_SYNC_AGAIN = 5 * ONE_MINUTE
    }

    val syncToBFSIDAllowed: LiveData<Boolean>
        get() = _syncToBFSIDAllowed
    private val _syncToBFSIDAllowed = MutableLiveData<Boolean>()

    val syncCardLiveData: LiveData<DashboardSyncCardState>
        get() = _syncCardLiveData
    private val _syncCardLiveData = MediatorLiveData<DashboardSyncCardState>()

    private val upSyncCountLiveData = MutableLiveData(0)
    private val syncStateLiveData = eventSyncManager.getLastSyncState()

    private suspend fun lastTimeSyncSucceed() = runBlocking {
        cacheSync.readLastSuccessfulSyncTime()
            ?.let { timeHelper.readableBetweenNowAndTime(it) }
    }

    private var lastTimeSyncRun: Date? = null
    private var estimatedOutage: Long? = null

    init {
        viewModelScope.launch {
            _syncCardLiveData.postValue(SyncConnecting(lastTimeSyncSucceed(), 0, null))
        }

        startInitialSyncIfRequired()
        load()
    }

    fun sync() {
        _syncCardLiveData.postValue(SyncConnecting(null, 0, null))
        eventSyncManager.sync()
    }

    private fun startInitialSyncIfRequired() {
        viewModelScope.launch {
            val lastUpdate = lastTimeSyncRun ?: cacheSync.readLastSuccessfulSyncTime()

            val isRunning = syncStateLiveData.value?.isSyncRunning() ?: false

            if (!isRunning && (lastUpdate == null || timeHelper.msBetweenNowAndTime(lastUpdate.time) > MAX_TIME_BEFORE_SYNC_AGAIN)) {
                sync()
            }
        }
    }

    private fun load() =
        viewModelScope.launch {
            _syncCardLiveData.addSource(deviceManager.isConnectedLiveData) {
                CoroutineScope(coroutineContext + SupervisorJob()).launch {
                    emitNewCardState(
                        it,
                        isModuleSelectionRequired(),
                        syncStateLiveData.value,
                        upSyncCountLiveData.value ?: 0,
                    )
                }
            }
            _syncCardLiveData.addSource(syncStateLiveData) {
                CoroutineScope(coroutineContext + SupervisorJob()).launch {
                    emitNewCardState(
                        isConnected(),
                        isModuleSelectionRequired(),
                        it,
                        upSyncCountLiveData.value ?: 0,
                    )
                }
            }
            _syncCardLiveData.addSource(upSyncCountLiveData) {
                CoroutineScope(coroutineContext + SupervisorJob()).launch {
                    emitNewCardState(
                        isConnected(),
                        isModuleSelectionRequired(),
                        syncStateLiveData.value,
                        it
                    )
                }
            }
            configManager.getProjectConfiguration().also { configuration ->
                _syncToBFSIDAllowed.postValue(configuration.canSyncDataToSimprints() || configuration.isEventDownSyncAllowed())
            }
            eventRepository
                .observeLocalCount(loginManager.getSignedInProjectIdOrEmpty(), EventType.ENROLMENT_V2)
                .collect { upSyncCountLiveData.postValue(it) }
        }

    private suspend fun emitNewCardState(
        isConnected: Boolean,
        isModuleSelectionRequired: Boolean,
        syncState: EventSyncState?,
        itemsToUpSync: Int,
    ) {
        val syncRunningAndInfoNotReadyYet =
            syncState == null && _syncCardLiveData.value is SyncConnecting
        val syncNotRunningAndInfoNotReadyYet =
            syncState == null && _syncCardLiveData.value !is SyncConnecting

        when {
            isModuleSelectionRequired -> SyncHasNoModules(lastTimeSyncSucceed())
            !isConnected -> SyncOffline(lastTimeSyncSucceed())
            syncRunningAndInfoNotReadyYet -> SyncConnecting(lastTimeSyncSucceed(), 0, null)
            syncNotRunningAndInfoNotReadyYet -> SyncDefault(lastTimeSyncSucceed())
            syncState == null -> SyncDefault(null) //Useless after the 2 above - just to satisfy nullability in the else
            else -> processRecentSyncState(syncState, itemsToUpSync)
        }.let {
            _syncCardLiveData.postValue(it)
            if (syncState != null && syncState.isSyncRunning()) {
                lastTimeSyncRun = Date()
            }
        }
    }

    private suspend fun processRecentSyncState(syncState: EventSyncState, itemsToUpSync: Int): DashboardSyncCardState {

        val downSyncStates = syncState.downSyncWorkersInfo
        val upSyncStates = syncState.upSyncWorkersInfo
        val allSyncStates = downSyncStates + upSyncStates

        return when {
            isThereNotSyncHistory(allSyncStates) -> SyncDefault(lastTimeSyncSucceed())
            isSyncCompleted(allSyncStates) -> {
                if (itemsToUpSync == 0) SyncComplete(lastTimeSyncSucceed())
                else SyncPendingUpload(lastTimeSyncSucceed(), itemsToUpSync)
            }
            isSyncProcess(allSyncStates) -> SyncProgress(
                lastTimeSyncSucceed(),
                syncState.progress,
                syncState.total
            )
            isSyncConnecting(allSyncStates) -> SyncConnecting(
                lastTimeSyncSucceed(),
                syncState.progress,
                syncState.total
            )
            isSyncFailedBecauseTooManyRequests(allSyncStates) -> SyncTooManyRequests(
                lastTimeSyncSucceed()
            )
            isSyncFailedBecauseCloudIntegration(allSyncStates) -> SyncFailed(lastTimeSyncSucceed())
            isSyncFailedBecauseBackendMaintenance(allSyncStates) -> SyncFailedBackendMaintenance(
                lastTimeSyncSucceed(),
                estimatedOutage
            )
            isSyncFailed(allSyncStates) -> SyncTryAgain(lastTimeSyncSucceed())
            else -> SyncProgress(lastTimeSyncSucceed(), syncState.progress, syncState.total)
        }
    }

    private fun isThereNotSyncHistory(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.isEmpty()

    private fun isSyncCompleted(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.all { it.state is EventSyncWorkerState.Succeeded }

    private fun isSyncProcess(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is EventSyncWorkerState.Running }

    private fun isSyncConnecting(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is EventSyncWorkerState.Enqueued }

    private fun isSyncFailedBecauseTooManyRequests(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is EventSyncWorkerState.Failed && (it.state as EventSyncWorkerState.Failed).failedBecauseTooManyRequest }

    private fun isSyncFailedBecauseCloudIntegration(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is EventSyncWorkerState.Failed && (it.state as EventSyncWorkerState.Failed).failedBecauseCloudIntegration }

    private fun isSyncFailedBecauseBackendMaintenance(allSyncStates: List<EventSyncState.SyncWorkerInfo>): Boolean {
        val isBackendMaintenance =
            allSyncStates.any { it.state is EventSyncWorkerState.Failed && (it.state as EventSyncWorkerState.Failed).failedBecauseBackendMaintenance }
        if (isBackendMaintenance) {
            val syncWorkerInfo =
                allSyncStates.find { it.state is EventSyncWorkerState.Failed && (it.state as EventSyncWorkerState.Failed).estimatedOutage != 0L }
            val failedWorkerState = syncWorkerInfo?.state as EventSyncWorkerState.Failed?
            estimatedOutage = failedWorkerState?.estimatedOutage
        }
        return isBackendMaintenance
    }

    private fun isSyncFailed(allSyncStates: List<EventSyncState.SyncWorkerInfo>) =
        allSyncStates.any { it.state is EventSyncWorkerState.Failed || it.state is EventSyncWorkerState.Blocked || it.state is EventSyncWorkerState.Cancelled }

    private suspend fun isModuleSelectionRequired() =
        isDownSyncAllowed() && isSelectedModulesEmpty() && isModuleSync()

    private suspend fun isDownSyncAllowed() =
        configManager.getProjectConfiguration().synchronization.frequency != SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC

    private suspend fun isSelectedModulesEmpty() =
        configManager.getDeviceConfiguration().selectedModules.isEmpty()

    private suspend fun isModuleSync() =
        configManager.getProjectConfiguration().synchronization.down.partitionType == DownSynchronizationConfiguration.PartitionType.MODULE

    private fun isConnected() = deviceManager.isConnectedLiveData.value ?: true

}

