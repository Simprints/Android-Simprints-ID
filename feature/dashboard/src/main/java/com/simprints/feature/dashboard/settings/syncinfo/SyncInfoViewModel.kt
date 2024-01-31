package com.simprints.feature.dashboard.settings.syncinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.models.isEventDownSyncAllowed
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SyncInfoViewModel @Inject constructor(
    private val rootManager: SecurityManager,
    private val configRepository: ConfigRepository,
    connectivityTracker: ConnectivityTracker,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val authStore: AuthStore,
    private val imageRepository: ImageRepository,
    private val eventSyncManager: EventSyncManager,
    private val tokenizationProcessor: TokenizationProcessor
) : ViewModel() {

    val recordsInLocal: LiveData<Int?>
        get() = _recordsInLocal
    private val _recordsInLocal = MutableLiveData<Int?>(null)

    val recordsToUpSync: LiveData<Int?>
        get() = _recordsToUpSync
    private val _recordsToUpSync = MutableLiveData<Int?>(null)

    val imagesToUpload: LiveData<Int?>
        get() = _imagesToUpload
    private val _imagesToUpload = MutableLiveData<Int?>(null)

    val recordsToDownSync: LiveData<Int?>
        get() = _recordsToDownSync
    private val _recordsToDownSync = MutableLiveData<Int?>(null)

    val recordsToDelete: LiveData<Int?>
        get() = _recordsToDelete
    private val _recordsToDelete = MutableLiveData<Int?>(null)

    val moduleCounts: LiveData<List<ModuleCount>>
        get() = _moduleCounts
    private val _moduleCounts = MutableLiveData<List<ModuleCount>>()

    val configuration: LiveData<ProjectConfiguration>
        get() = _configuration
    private val _configuration = MutableLiveData<ProjectConfiguration>()

    val isConnected: LiveData<Boolean> = connectivityTracker.observeIsConnected()

    val lastSyncState = eventSyncManager.getLastSyncState()
    private var lastKnownEventSyncState: EventSyncState? = null

    val isSyncAvailable: LiveData<Boolean>
        get() = _isSyncAvailable
    private val _isSyncAvailable = MediatorLiveData<Boolean>()

    init {
        _isSyncAvailable.addSource(lastSyncState) { lastSyncStateValue ->
            _isSyncAvailable.postValue(
                emitSyncAvailable(
                    isSyncRunning = lastSyncStateValue?.isSyncRunning(),
                    isConnected = isConnected.value,
                    syncConfiguration = configuration.value?.synchronization,
                )
            )
        }
        _isSyncAvailable.addSource(isConnected) { isConnectedValue ->
            _isSyncAvailable.postValue(
                emitSyncAvailable(
                    isSyncRunning = lastSyncState.value?.isSyncRunning(),
                    isConnected = isConnectedValue,
                    syncConfiguration = configuration.value?.synchronization,
                )
            )
        }
        _isSyncAvailable.addSource(_configuration) { config ->
            _isSyncAvailable.postValue(
                emitSyncAvailable(
                    isSyncRunning = lastSyncState.value?.isSyncRunning(),
                    isConnected = isConnected.value,
                    syncConfiguration = config.synchronization,
                )
            )
        }
    }

    fun refreshInformation() {
        _recordsInLocal.postValue(null)
        _recordsToUpSync.postValue(null)
        _recordsToDownSync.postValue(null)
        _recordsToDelete.postValue(null)
        _imagesToUpload.postValue(null)
        _moduleCounts.postValue(listOf())
        load()
    }

    fun forceSync() {
        eventSyncManager.sync()
        // There is a delay between starting sync and lastSyncState
        // reporting it so this prevents starting multiple syncs by accident
        _isSyncAvailable.postValue(false)
    }

    /**
     * Calls fetchSyncInformation() when all workers are done.
     * To determine this EventSyncState is checked to have all workers in Succeeded state.
     * Also, to avoid consecutive calls with the same EventSyncState the last one is saved
     * and compared with new one before evaluating it.
     */
    fun fetchSyncInformationIfNeeded(eventSyncState: EventSyncState) {
        if (eventSyncState != lastKnownEventSyncState) {
            val workers = eventSyncState.downSyncWorkersInfo + eventSyncState.upSyncWorkersInfo
            val unfinishedWorkers = workers.filter { it.state != EventSyncWorkerState.Succeeded }
            if (unfinishedWorkers.isEmpty()) {
                load()
            }

            lastKnownEventSyncState = eventSyncState
        }
    }

    private fun load() = viewModelScope.launch {
        // if device is not safe stop all sync operations
        // users can root the device after login and we need to stop sync operations
        if (isDeviceSafe().not()) {
            return@launch
        }

        val projectId = authStore.signedInProjectId

        awaitAll(
            async { _configuration.postValue(configRepository.getProjectConfiguration()) },
            async { _recordsInLocal.postValue(getRecordsInLocal(projectId)) },
            async { _recordsToUpSync.postValue(getRecordsToUpSync(projectId)) },
            async {
                fetchRecordsToCreateAndDeleteCount().let {
                    _recordsToDownSync.postValue(it.toCreate)
                    _recordsToDelete.postValue(it.toDelete)
                }
            },
            async { _imagesToUpload.postValue(imageRepository.getNumberOfImagesToUpload(projectId)) },
            async { _moduleCounts.postValue(getModuleCounts(projectId)) }
        )
    }

    fun isDeviceSafe(): Boolean = try {
        rootManager.checkIfDeviceIsRooted()
        true
    } catch (e: RootedDeviceException) {
        Simber.e(e)
        false
    }

    private fun emitSyncAvailable(
        isSyncRunning: Boolean?,
        isConnected: Boolean?,
        syncConfiguration: SynchronizationConfiguration? = configuration.value?.synchronization,
    ) = isConnected == true
        && isSyncRunning == false
        && syncConfiguration?.let {
        !isModuleSync(it.down) || isModuleSyncAndModuleIdOptionsNotEmpty(
            it
        )
    } == true

    private fun isModuleSync(syncConfiguration: DownSynchronizationConfiguration) =
        syncConfiguration.partitionType == DownSynchronizationConfiguration.PartitionType.MODULE

    fun isModuleSyncAndModuleIdOptionsNotEmpty(synchronizationConfiguration: SynchronizationConfiguration) =
        synchronizationConfiguration.down.let { it.moduleOptions.isNotEmpty() && isModuleSync(it) }

    private suspend fun getRecordsInLocal(projectId: String): Int =
        enrolmentRecordRepository.count(SubjectQuery(projectId = projectId))

    private suspend fun getRecordsToUpSync(projectId: String): Int =
        eventSyncManager.countEventsToUpload(projectId, EventType.ENROLMENT_V2)
            .firstOrNull()
            ?: 0

    private suspend fun fetchRecordsToCreateAndDeleteCount(): DownSyncCounts =
        if (configRepository.getProjectConfiguration().isEventDownSyncAllowed()) {
            fetchAndUpdateRecordsToDownSyncAndDeleteCount()
        } else {
            DownSyncCounts(0, 0)
        }

    private suspend fun fetchAndUpdateRecordsToDownSyncAndDeleteCount(): DownSyncCounts =
        try {
            eventSyncManager.countEventsToDownload()
        } catch (t: Throwable) {
            Simber.d(t)
            DownSyncCounts(0, 0)
        }

    private suspend fun getModuleCounts(projectId: String): List<ModuleCount> =
        configRepository.getDeviceConfiguration().selectedModules.map { moduleName ->
            val count = enrolmentRecordRepository.count(
                SubjectQuery(projectId = projectId, moduleId = moduleName.value)
            )
            val decryptedName = when (moduleName) {
                is TokenizableString.Raw -> moduleName
                is TokenizableString.Tokenized -> tokenizationProcessor.decrypt(
                    encrypted = moduleName,
                    tokenKeyType = TokenKeyType.ModuleId,
                    project = configRepository.getProject(projectId)
                )
            }
            return@map ModuleCount(name = decryptedName.value, count = count)
        }

}
