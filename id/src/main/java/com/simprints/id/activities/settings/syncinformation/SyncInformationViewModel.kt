package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity.ViewState.LoadingState
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity.ViewState.SyncDataFetched
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_DELETION
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.EXTRA
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.ON
import com.simprints.id.services.sync.events.master.models.EventSyncState
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerState
import kotlinx.coroutines.launch
import timber.log.Timber

class SyncInformationViewModel(
    private val downySyncHelper: EventDownSyncHelper,
    private val eventRepository: EventRepository,
    private val subjectRepository: SubjectRepository,
    private val preferencesManager: PreferencesManager,
    private val projectId: String,
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    private val imageRepository: ImageRepository,
    private val eventSyncManager: EventSyncManager
) : ViewModel() {

    private val _viewState: MutableLiveData<SyncInformationActivity.ViewState> = MutableLiveData()
    fun getViewStateLiveData(): LiveData<SyncInformationActivity.ViewState> = _viewState

    fun fetchSyncInformation() = viewModelScope.launch {
        // If a sync was never triggered than it will return null - in Co-Sync only for example
        val isRunning = eventSyncManager.getLastSyncState().value?.isRunning() ?: false

        if (isRunning) {
            _viewState.value = LoadingState.Syncing
        } else {
            _viewState.value = LoadingState.Calculating
            _viewState.value = fetchRecords()
        }
    }

    private suspend fun fetchRecords(): SyncDataFetched {
        val subjectCounts = fetchRecordsToCreateAndDeleteCountOrNull()
        return SyncDataFetched(
            recordsInLocal = fetchLocalRecordCount(),
            recordsToDownSync = subjectCounts?.toCreate ?: 0,
            recordsToUpSync = fetchAndUpdateRecordsToUpSyncCount(),
            recordsToDelete = subjectCounts?.toDelete ?: 0,
            imagesToUpload = fetchAndUpdateImagesToUploadCount(),
            moduleCounts = fetchAndUpdateSelectedModulesCount()
        )
    }

    private suspend fun fetchLocalRecordCount() =
        subjectRepository.count(SubjectQuery(projectId = projectId))

    private fun fetchAndUpdateImagesToUploadCount() = imageRepository.getNumberOfImagesToUpload()

    private suspend fun fetchAndUpdateRecordsToUpSyncCount() =
        eventRepository.localCount(projectId = projectId, type = ENROLMENT_RECORD_CREATION)

    private suspend fun fetchRecordsToCreateAndDeleteCountOrNull(): DownSyncCounts? =
        if (isDownSyncAllowed() && canSyncToSimprints()) {
            fetchAndUpdateRecordsToDownSyncAndDeleteCount()
        } else {
            null
        }

    private fun isDownSyncAllowed() =
        with(preferencesManager) {
            eventDownSyncSetting == ON || eventDownSyncSetting == EXTRA
        }

    private fun canSyncToSimprints(): Boolean = with(preferencesManager) {
        syncDestinationSettings.contains(SyncDestinationSetting.SIMPRINTS)
    }

    private suspend fun fetchAndUpdateRecordsToDownSyncAndDeleteCount(): DownSyncCounts? =
        try {
            val downSyncScope = eventDownSyncScopeRepository.getDownSyncScope()
            var creationsToDownload = 0
            var deletionsToDownload = 0

            downSyncScope.operations.forEach {
                val counts = downySyncHelper.countForDownSync(it)
                creationsToDownload += counts.firstOrNull { it.type == ENROLMENT_RECORD_CREATION }
                    ?.count ?: 0
                deletionsToDownload += counts.firstOrNull { it.type == ENROLMENT_RECORD_DELETION }
                    ?.count ?: 0
            }

            DownSyncCounts(creationsToDownload, deletionsToDownload)

        } catch (t: Throwable) {
            Timber.d(t)
            null
        }

    private suspend fun fetchAndUpdateSelectedModulesCount() =
        preferencesManager.selectedModules.map {
            ModuleCount(
                it,
                subjectRepository.count(SubjectQuery(projectId = projectId, moduleId = it))
            )
        }


    private fun EventSyncState.isRunning(): Boolean {
        val downSyncStates = downSyncWorkersInfo
        val upSyncStates = upSyncWorkersInfo
        val allSyncStates = downSyncStates + upSyncStates
        return allSyncStates.any {
            it.state is EventSyncWorkerState.Running || it.state is EventSyncWorkerState.Enqueued
        }
    }

    data class DownSyncCounts(val toCreate: Int, val toDelete: Int)
}
