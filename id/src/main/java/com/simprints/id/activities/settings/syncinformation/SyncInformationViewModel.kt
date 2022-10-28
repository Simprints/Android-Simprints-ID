package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.EventType.*
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.models.EventSyncState
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState
import com.simprints.id.tools.extensions.toGroup
import com.simprints.id.tools.extensions.toMode
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncInformationViewModel @Inject constructor(
    private val downySyncHelper: EventDownSyncHelper,
    private val eventRepository: EventRepository,
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val loginManager: LoginManager,
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    private val imageRepository: ImageRepository,
    private val configManager: ConfigManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    val recordsInLocal = MutableLiveData<Int?>(null)
    val recordsToUpSync = MutableLiveData<Int?>(null)
    val imagesToUpload = MutableLiveData<Int?>(null)
    val recordsToDownSync = MutableLiveData<Int?>(null)
    val recordsToDelete = MutableLiveData<Int?>(null)
    val moduleCounts = MutableLiveData<List<ModuleCount>?>(null)
    val synchronizationConfiguration = MutableLiveData<SynchronizationConfiguration>()

    private var lastKnownEventSyncState: EventSyncState? = null

    init {
        viewModelScope.launch(dispatcher) {
            synchronizationConfiguration.postValue(configManager.getProjectConfiguration().synchronization)
        }
    }

    /**
     * Calls fetchSyncInformation() when all workers are done.
     * To determine this EventSyncState is checked to have all workers in Succeeded state.
     * Also, to avoid consecutive calls with the same EventSyncState the last one is saved
     * and compared with new one before evaluating it.
     */
    fun fetchSyncInformationIfNeeded(eventSyncState: EventSyncState) {
        if (eventSyncState != lastKnownEventSyncState) {
            val unfinishedDownSyncWorkers = eventSyncState.downSyncWorkersInfo.filter {
                (it.state != EventSyncWorkerState.Succeeded)
            }
            val unfinishedUpSyncWorkers = eventSyncState.upSyncWorkersInfo.filter {
                (it.state != EventSyncWorkerState.Succeeded)
            }
            val unfinishedWorkers = unfinishedDownSyncWorkers + unfinishedUpSyncWorkers
            if (unfinishedWorkers.isEmpty()) {
                fetchSyncInformation()
            }

            lastKnownEventSyncState = eventSyncState
        }
    }

    fun fetchSyncInformation() {
        viewModelScope.launch(dispatcher) {
            recordsInLocal.postValue(fetchLocalRecordCount())
            recordsToUpSync.postValue(fetchAndUpdateRecordsToUpSyncCount())
            fetchRecordsToCreateAndDeleteCountOrNull().let {
                recordsToDownSync.postValue(it?.toCreate ?: 0)
                recordsToDelete.postValue(it?.toDelete ?: 0)
            }
            // Move walking the file system to the IO thread
            imagesToUpload.postValue(fetchAndUpdateImagesToUploadCount())
            moduleCounts.postValue(fetchAndUpdateSelectedModulesCount())
        }
    }

    /**
     * This function cancels any previous coroutines fetching sync data, and resets the record
     * values. This allows the user to hard reset any stuck http requests / disk reads etc.
     */
    fun resetFetchingSyncInformation() {
        viewModelScope.coroutineContext.cancelChildren()
        recordsInLocal.postValue(null)
        recordsToUpSync.postValue(null)
        imagesToUpload.postValue(null)
        recordsToDownSync.postValue(null)
        recordsToDelete.postValue(null)
        moduleCounts.postValue(null)
    }

    private suspend fun fetchLocalRecordCount() =
        enrolmentRecordManager.count(SubjectQuery(projectId = loginManager.getSignedInProjectIdOrEmpty()))

    private fun fetchAndUpdateImagesToUploadCount() = imageRepository.getNumberOfImagesToUpload()

    private suspend fun fetchAndUpdateRecordsToUpSyncCount() =
        eventRepository.localCount(
            projectId = loginManager.getSignedInProjectIdOrEmpty(),
            type = ENROLMENT_V2
        ) +
            // TODO remove. This is needed because of events created before 2021.1. Once all users update to 2021.1+ we can remove it
            eventRepository.localCount(
                projectId = loginManager.getSignedInProjectIdOrEmpty(),
                type = ENROLMENT_RECORD_CREATION
            )

    private suspend fun fetchRecordsToCreateAndDeleteCountOrNull(): DownSyncCounts? =
        if (isDownSyncAllowed()) {
            fetchAndUpdateRecordsToDownSyncAndDeleteCount()
        } else {
            null
        }

    private suspend fun isDownSyncAllowed() =
        configManager.getProjectConfiguration().synchronization.frequency != SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC

    private suspend fun fetchAndUpdateRecordsToDownSyncAndDeleteCount(): DownSyncCounts? =
        try {
            val projectConfig = configManager.getProjectConfiguration()
            val deviceConfig = configManager.getDeviceConfiguration()
            val downSyncScope = eventDownSyncScopeRepository.getDownSyncScope(
                projectConfig.general.modalities.map { it.toMode() },
                deviceConfig.selectedModules,
                projectConfig.synchronization.down.partitionType.toGroup()
            )
            var creationsToDownload = 0
            var deletionsToDownload = 0

            downSyncScope.operations.forEach { syncOperation ->
                val counts = downySyncHelper.countForDownSync(syncOperation)
                creationsToDownload += counts.firstOrNull { it.type == ENROLMENT_RECORD_CREATION }
                    ?.count ?: 0
                deletionsToDownload += counts.firstOrNull { it.type == ENROLMENT_RECORD_DELETION }
                    ?.count ?: 0
            }

            DownSyncCounts(creationsToDownload, deletionsToDownload)

        } catch (t: Throwable) {
            Simber.d(t)
            null
        }

    private suspend fun fetchAndUpdateSelectedModulesCount() =
        configManager.getDeviceConfiguration().selectedModules.map {
            ModuleCount(
                it,
                enrolmentRecordManager.count(
                    SubjectQuery(
                        projectId = loginManager.getSignedInProjectIdOrEmpty(),
                        moduleId = it
                    )
                )
            )
        }

    data class DownSyncCounts(val toCreate: Int, val toDelete: Int)
}
