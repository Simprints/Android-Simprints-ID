package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_DELETION
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation
import com.simprints.id.data.db.events_sync.up.domain.LocalEventQuery
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.EXTRA
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.ON
import com.simprints.id.services.sync.events.up.EventUpSyncHelper
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class SyncInformationViewModel(private val downySyncHelper: EventDownSyncHelper,
                               private val upSyncHelper: EventUpSyncHelper,
                               private val subjectRepository: SubjectRepository,
                               private val preferencesManager: PreferencesManager,
                               private val projectId: String,
                               private val downSyncScopeRepository: EventDownSyncScopeRepository) : ViewModel() {

    val localRecordCountLiveData = MutableLiveData<Int>()
    val recordsToUpSyncCountLiveData = MutableLiveData<Int>()
    val recordsToDownSyncCountLiveData = MutableLiveData<Int>()
    val recordsToDeleteCountLiveData = MutableLiveData<Int>()
    val selectedModulesCountLiveData = MutableLiveData<List<ModuleCount>>()
    val unselectedModulesCountLiveData = MutableLiveData<List<ModuleCount>>()

    fun fetchRecordsInfo() {
        viewModelScope.launch {
            fetchAndUpdateLocalRecordCount()
            fetchAndUpdateRecordsToUpSyncCount()
            fetchRecordsToUpdateAndDeleteCountIfNecessary()
            fetchAndUpdateSelectedModulesCount()
            fetchAndUpdatedUnselectedModulesCount()
        }
    }

    internal suspend fun fetchAndUpdateLocalRecordCount() {
        localRecordCountLiveData.value = subjectRepository.count(SubjectQuery(projectId = projectId))
    }

    internal suspend fun fetchAndUpdateRecordsToUpSyncCount() {
        recordsToUpSyncCountLiveData.value =
            upSyncHelper.countForUpSync(EventUpSyncOperation(LocalEventQuery(projectId = projectId, type = ENROLMENT_RECORD_CREATION)))
    }

    internal suspend fun fetchRecordsToUpdateAndDeleteCountIfNecessary() {
        if (isDownSyncAllowed()) {
            fetchAndUpdateRecordsToDownSyncAndDeleteCount()
        }
    }

    private fun isDownSyncAllowed() = with(preferencesManager) {
        eventDownSyncSetting == ON || eventDownSyncSetting == EXTRA
    }

    internal suspend fun fetchAndUpdateRecordsToDownSyncAndDeleteCount() {
        try {
            val downSyncScope = downSyncScopeRepository.getDownSyncScope()
            var creationsToDownload = 0
            var deletionsToDownload = 0

            downSyncScope.operations.forEach {
                val counts = downySyncHelper.countForDownSync(it)
                creationsToDownload += counts.firstOrNull { it.type == ENROLMENT_RECORD_CREATION }?.count
                    ?: 0
                deletionsToDownload += counts.firstOrNull { it.type == ENROLMENT_RECORD_DELETION }?.count
                    ?: 0
            }

            recordsToDownSyncCountLiveData.postValue(creationsToDownload)
            recordsToDeleteCountLiveData.postValue(deletionsToDownload)

        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    internal suspend fun fetchAndUpdateSelectedModulesCount() {
        selectedModulesCountLiveData.value = preferencesManager.selectedModules.map {
            ModuleCount(it,
                subjectRepository.count(SubjectQuery(projectId = projectId, moduleId = it)))
        }
    }

    internal suspend fun fetchAndUpdatedUnselectedModulesCount() {
        val unselectedModules = subjectRepository.load(
            SubjectQuery(projectId = projectId)
        ).filter { !preferencesManager.selectedModules.contains(it.moduleId) }
            .toList()
            .groupBy { it.moduleId }

        val unselectedModulesWithCount = unselectedModules.map {
            ModuleCount(it.key, it.value.size)
        }

        unselectedModulesCountLiveData.value = unselectedModulesWithCount
    }
}
