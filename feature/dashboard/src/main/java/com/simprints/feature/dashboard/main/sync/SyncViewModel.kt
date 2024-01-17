package com.simprints.feature.dashboard.main.sync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.feature.dashboard.views.SyncCardState
import com.simprints.feature.dashboard.views.SyncCardState.SyncComplete
import com.simprints.feature.dashboard.views.SyncCardState.SyncConnecting
import com.simprints.feature.dashboard.views.SyncCardState.SyncDefault
import com.simprints.feature.dashboard.views.SyncCardState.SyncFailed
import com.simprints.feature.dashboard.views.SyncCardState.SyncFailedBackendMaintenance
import com.simprints.feature.dashboard.views.SyncCardState.SyncHasNoModules
import com.simprints.feature.dashboard.views.SyncCardState.SyncOffline
import com.simprints.feature.dashboard.views.SyncCardState.SyncPendingUpload
import com.simprints.feature.dashboard.views.SyncCardState.SyncProgress
import com.simprints.feature.dashboard.views.SyncCardState.SyncTooManyRequests
import com.simprints.feature.dashboard.views.SyncCardState.SyncTryAgain
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.store.models.isEventDownSyncAllowed
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.network.ConnectivityTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date
import javax.inject.Inject

@HiltViewModel
internal class SyncViewModel @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val connectivityTracker: ConnectivityTracker,
    private val configRepository: ConfigRepository,
    private val timeHelper: TimeHelper,
    private val authStore: AuthStore,
    private val logoutUseCase: LogoutUseCase,
    @ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {

    companion object {

        private const val ONE_MINUTE = 1000 * 60L
        private const val MAX_TIME_BEFORE_SYNC_AGAIN = 5 * ONE_MINUTE
    }

    val syncToBFSIDAllowed: LiveData<Boolean>
        get() = _syncToBFSIDAllowed
    private val _syncToBFSIDAllowed = MutableLiveData<Boolean>()

    val syncCardLiveData: LiveData<SyncCardState>
        get() = _syncCardLiveData
    private val _syncCardLiveData = MediatorLiveData<SyncCardState>()
    val signOutEventLiveData: LiveData<LiveDataEvent>
        get() = _signOutEventLiveData
    private val _signOutEventLiveData = MediatorLiveData<LiveDataEvent>()

    private val upSyncCountLiveData = MutableLiveData(0)
    private val syncStateLiveData = eventSyncManager.getLastSyncState()

    private suspend fun lastTimeSyncSucceed() = runBlocking {
        eventSyncManager.getLastSyncTime()
            ?.let { timeHelper.readableBetweenNowAndTime(it) }
    }

    private var lastTimeSyncRun: Date? = null
    private var estimatedOutage: Long? = null

    init {
        viewModelScope.launch {
            _syncCardLiveData.postValue(SyncConnecting(lastTimeSyncSucceed(), 0, null))
        }

        // CORE-2638
        // When project is in ENDING state and all data is synchronized, the user must be logged out
        _signOutEventLiveData.addSource(_syncCardLiveData) { cardState ->
            viewModelScope.launch {
                val isSyncComplete = cardState is SyncComplete
                val isProjectEnding =
                    configRepository.getProject(authStore.signedInProjectId).state == ProjectState.PROJECT_ENDING

                if (isSyncComplete && isProjectEnding) {
                    externalScope.launch {
                        logoutUseCase()
                        _signOutEventLiveData.postValue(LiveDataEvent())
                    }
                }
            }
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
            val lastUpdate = lastTimeSyncRun ?:  eventSyncManager.getLastSyncTime()

            val isRunning = syncStateLiveData.value?.isSyncRunning() ?: false

            if (!isRunning && (lastUpdate == null || timeHelper.msBetweenNowAndTime(lastUpdate.time) > MAX_TIME_BEFORE_SYNC_AGAIN)) {
                sync()
            }
        }
    }

    private fun load() =
        viewModelScope.launch {
            _syncCardLiveData.addSource(connectivityTracker.observeIsConnected()) {
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
            configRepository.getProjectConfiguration().also { configuration ->
                _syncToBFSIDAllowed.postValue(configuration.canSyncDataToSimprints() || configuration.isEventDownSyncAllowed())
            }
            eventSyncManager
                .countEventsToUpload(authStore.signedInProjectId, EventType.ENROLMENT_V2)
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

    private suspend fun processRecentSyncState(syncState: EventSyncState, itemsToUpSync: Int): SyncCardState {

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
        configRepository.getProjectConfiguration().synchronization.frequency != SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC

    private suspend fun isSelectedModulesEmpty() =
        configRepository.getDeviceConfiguration().selectedModules.isEmpty()

    private suspend fun isModuleSync() =
        configRepository.getProjectConfiguration().synchronization.down.partitionType == DownSynchronizationConfiguration.PartitionType.MODULE

    private fun isConnected() = connectivityTracker.observeIsConnected().value ?: true

}

