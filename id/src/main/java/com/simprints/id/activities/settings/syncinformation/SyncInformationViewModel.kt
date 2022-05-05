package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.modality.toMode
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_DELETION
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_V2
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.settings.canSyncDataToSimprints
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.EXTRA
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.ON
import com.simprints.logging.Simber
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class SyncInformationViewModel(
    private val downySyncHelper: EventDownSyncHelper,
    private val eventRepository: com.simprints.eventsystem.event.EventRepository,
    private val subjectRepository: SubjectRepository,
    private val preferencesManager: IdPreferencesManager,
    private val projectId: String,
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    private val imageRepository: ImageRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    val recordsInLocal = MutableLiveData<Int?>(null)
    val recordsToUpSync = MutableLiveData<Int?>(null)
    val imagesToUpload = MutableLiveData<Int?>(null)
    val recordsToDownSync = MutableLiveData<Int?>(null)
    val recordsToDelete = MutableLiveData<Int?>(null)
    val moduleCounts = MutableLiveData<List<ModuleCount>?>(null)

    fun fetchSyncInformation() {
        viewModelScope.launch { recordsInLocal.value = fetchLocalRecordCount() }
        viewModelScope.launch { recordsToUpSync.value = fetchAndUpdateRecordsToUpSyncCount() }
        viewModelScope.launch {
            fetchRecordsToCreateAndDeleteCountOrNull().let {
                recordsToDownSync.value = it?.toCreate ?: 0
                recordsToDelete.value = it?.toDelete ?: 0
            }
        }
        // Move walking the file system to the IO thread
        viewModelScope.launch(dispatchers.io()) {
            imagesToUpload.postValue(fetchAndUpdateImagesToUploadCount())
        }
        viewModelScope.launch { recordsInLocal.value = fetchLocalRecordCount() }
        viewModelScope.launch { moduleCounts.value = fetchAndUpdateSelectedModulesCount() }
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
        subjectRepository.count(SubjectQuery(projectId = projectId))

    private fun fetchAndUpdateImagesToUploadCount() = imageRepository.getNumberOfImagesToUpload()

    private suspend fun fetchAndUpdateRecordsToUpSyncCount() =
        eventRepository.localCount(projectId = projectId, type = ENROLMENT_V2) +
            // this is needed because of events created before 2021.1. Once all users update to 2021.1+ we can remove it
            eventRepository.localCount(projectId = projectId, type = ENROLMENT_RECORD_CREATION)

    private suspend fun fetchRecordsToCreateAndDeleteCountOrNull(): DownSyncCounts? =
        if (isDownSyncAllowed() && preferencesManager.canSyncDataToSimprints()) {
            fetchAndUpdateRecordsToDownSyncAndDeleteCount()
        } else {
            null
        }

    private fun isDownSyncAllowed() =
        with(preferencesManager) {
            eventDownSyncSetting == ON || eventDownSyncSetting == EXTRA
        }

    private suspend fun fetchAndUpdateRecordsToDownSyncAndDeleteCount(): DownSyncCounts? =
        try {
            val downSyncScope = eventDownSyncScopeRepository.getDownSyncScope(
                preferencesManager.modalities.map { it.toMode() },
                preferencesManager.selectedModules.toList(),
                preferencesManager.syncGroup
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
        preferencesManager.selectedModules.map {
            ModuleCount(
                it,
                subjectRepository.count(SubjectQuery(projectId = projectId, moduleId = it))
            )
        }

    data class DownSyncCounts(val toCreate: Int, val toDelete: Int)
}
