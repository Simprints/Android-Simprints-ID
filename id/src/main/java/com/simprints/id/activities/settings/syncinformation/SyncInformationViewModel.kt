package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.canSyncToSimprints
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.EXTRA
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.ON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class SyncInformationViewModel(
    private val downySyncHelper: EventDownSyncHelper,
    private val eventRepository: EventRepository,
    private val subjectRepository: SubjectRepository,
    private val preferencesManager: PreferencesManager,
    private val projectId: String,
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    private val imageRepository: ImageRepository
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
        viewModelScope.launch(Dispatchers.IO) {
            imagesToUpload.postValue(fetchAndUpdateImagesToUploadCount())
        }
        viewModelScope.launch { recordsInLocal.value = fetchLocalRecordCount() }
        viewModelScope.launch { moduleCounts.value = fetchAndUpdateSelectedModulesCount() }
    }

    private suspend fun fetchLocalRecordCount() =
        subjectRepository.count(SubjectQuery(projectId = projectId))

    private fun fetchAndUpdateImagesToUploadCount() = imageRepository.getNumberOfImagesToUpload()

    private suspend fun fetchAndUpdateRecordsToUpSyncCount() =
        eventRepository.localCount(projectId = projectId, type = ENROLMENT_V2)

    private suspend fun fetchRecordsToCreateAndDeleteCountOrNull(): DownSyncCounts? =
        if (isDownSyncAllowed() && preferencesManager.canSyncToSimprints()) {
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
            val downSyncScope = eventDownSyncScopeRepository.getDownSyncScope()
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

    data class DownSyncCounts(val toCreate: Int, val toDelete: Int)
}
