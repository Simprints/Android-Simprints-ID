package com.simprints.feature.dashboard.main.sync

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.feature.dashboard.views.SyncCardState
import com.simprints.feature.dashboard.views.SyncCardState.SyncComplete
import com.simprints.feature.dashboard.views.SyncCardState.SyncConnecting
import com.simprints.feature.dashboard.views.SyncCardState.SyncDefault
import com.simprints.feature.dashboard.views.SyncCardState.SyncFailed
import com.simprints.feature.dashboard.views.SyncCardState.SyncFailedBackendMaintenance
import com.simprints.feature.dashboard.views.SyncCardState.SyncFailedReloginRequired
import com.simprints.feature.dashboard.views.SyncCardState.SyncHasNoModules
import com.simprints.feature.dashboard.views.SyncCardState.SyncOffline
import com.simprints.feature.dashboard.views.SyncCardState.SyncPendingUpload
import com.simprints.feature.dashboard.views.SyncCardState.SyncProgress
import com.simprints.feature.dashboard.views.SyncCardState.SyncTooManyRequests
import com.simprints.feature.dashboard.views.SyncCardState.SyncTryAgain
import com.simprints.feature.login.LoginContract
import com.simprints.feature.login.LoginResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.store.models.isEventDownSyncAllowed
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.SyncOrchestrator
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
    private val syncOrchestrator: SyncOrchestrator,
    private val connectivityTracker: ConnectivityTracker,
    private val configManager: ConfigManager,
    private val timeHelper: TimeHelper,
    private val authStore: AuthStore,
    private val logout: LogoutUseCase,
    private val recentUserActivityManager: RecentUserActivityManager,
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

    val loginRequestedEventLiveData: LiveData<LiveDataEventWithContent<Bundle>>
        get() = _loginRequestedEventLiveData
    private val _loginRequestedEventLiveData = MutableLiveData<LiveDataEventWithContent<Bundle>>()

    private val upSyncCountLiveData = MutableLiveData(0)
    private val syncStateLiveData = eventSyncManager.getLastSyncState()

    private suspend fun lastTimeSyncSucceed() = runBlocking {
        eventSyncManager.getLastSyncTime()
            ?.let { timeHelper.readableBetweenNowAndTime(it) }
    }

    private var lastTimeSyncRun: Date? = null

    init {
        viewModelScope.launch {
            _syncCardLiveData.postValue(SyncConnecting(lastTimeSyncSucceed(), 0, null))
        }

        // CORE-2638
        // When project is in ENDING state and all data is synchronized, the user must be logged out
        _signOutEventLiveData.addSource(_syncCardLiveData) { cardState ->
            viewModelScope.launch {
                val isSyncComplete = cardState is SyncComplete
                val isProjectEnding = try {
                    configManager.getProject(authStore.signedInProjectId).state == ProjectState.PROJECT_ENDING
                } catch (e: Throwable) {
                    // When the device is compromised the project data will be deleted and
                    // attempting to access project state with result in exception.
                    // For user it is essentially the same as project ending.
                    true
                }

                if (isSyncComplete && isProjectEnding) {
                    viewModelScope.launch {
                        logout()
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
        syncOrchestrator.startEventSync()
    }

    fun login() {
        viewModelScope.launch {
            val loginArgs = LoginContract.toArgs(
                authStore.signedInProjectId,
                authStore.signedInUserId
                    ?: recentUserActivityManager.getRecentUserActivity().lastUserUsed
            )
            _loginRequestedEventLiveData.send(loginArgs)
        }
    }

    fun handleLoginResult(result: LoginResult) {
        if (result.isSuccess) {
            sync()
        }
    }

    private fun startInitialSyncIfRequired() {
        viewModelScope.launch {
            val lastUpdate = lastTimeSyncRun ?: eventSyncManager.getLastSyncTime()

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
            configManager.getProjectConfiguration().also { configuration ->
                _syncToBFSIDAllowed.postValue(configuration.canSyncDataToSimprints() || configuration.isEventDownSyncAllowed())
            }
            eventSyncManager
                .countEventsToUpload(EventType.ENROLMENT_V2)
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

    private suspend fun processRecentSyncState(
        syncState: EventSyncState,
        itemsToUpSync: Int,
    ): SyncCardState {

        return when {
            syncState.isThereNotSyncHistory() -> SyncDefault(lastTimeSyncSucceed())
            syncState.isSyncCompleted() -> {
                if (itemsToUpSync == 0) SyncComplete(lastTimeSyncSucceed())
                else SyncPendingUpload(lastTimeSyncSucceed(), itemsToUpSync)
            }

            syncState.isSyncInProgress() -> SyncProgress(
                lastTimeSyncSucceed(),
                syncState.progress,
                syncState.total
            )

            syncState.isSyncConnecting() -> SyncConnecting(
                lastTimeSyncSucceed(),
                syncState.progress,
                syncState.total
            )

            syncState.isSyncFailedBecauseReloginRequired() -> SyncFailedReloginRequired(
                lastTimeSyncSucceed()
            )

            syncState.isSyncFailedBecauseTooManyRequests() -> SyncTooManyRequests(
                lastTimeSyncSucceed()
            )

            syncState.isSyncFailedBecauseCloudIntegration() -> SyncFailed(lastTimeSyncSucceed())
            syncState.isSyncFailedBecauseBackendMaintenance() -> SyncFailedBackendMaintenance(
                lastTimeSyncSucceed(),
                syncState.getEstimatedBackendMaintenanceOutage()
            )

            syncState.isSyncFailed() -> SyncTryAgain(lastTimeSyncSucceed())
            else -> SyncProgress(lastTimeSyncSucceed(), syncState.progress, syncState.total)
        }
    }


    private suspend fun isModuleSelectionRequired() =
        isDownSyncAllowed() && isSelectedModulesEmpty() && isModuleSync()

    private suspend fun isDownSyncAllowed() =
        configManager.getProjectConfiguration().synchronization.frequency != SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC

    private suspend fun isSelectedModulesEmpty() =
        configManager.getDeviceConfiguration().selectedModules.isEmpty()

    private suspend fun isModuleSync() =
        configManager.getProjectConfiguration().synchronization.down.partitionType == DownSynchronizationConfiguration.PartitionType.MODULE

    private fun isConnected() = connectivityTracker.observeIsConnected().value ?: true

}

