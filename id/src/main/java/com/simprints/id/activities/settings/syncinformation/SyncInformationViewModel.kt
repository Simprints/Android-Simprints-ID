package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.subjects.master.models.SubjectsDownSyncSetting.EXTRA
import com.simprints.id.services.sync.subjects.master.models.SubjectsDownSyncSetting.ON
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class SyncInformationViewModel(private val personRepository: SubjectRepository,
                               private val subjectLocalDataSource: SubjectLocalDataSource,
                               private val preferencesManager: PreferencesManager,
                               private val projectId: String,
                               private val subjectsDownSyncScopeRepository: SubjectsDownSyncScopeRepository) : ViewModel() {

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
        localRecordCountLiveData.value = subjectLocalDataSource.count(SubjectLocalDataSource.Query(projectId = projectId))
    }

    internal suspend fun fetchAndUpdateRecordsToUpSyncCount() {
        recordsToUpSyncCountLiveData.value = subjectLocalDataSource.count(SubjectLocalDataSource.Query(toSync = true))
    }

    internal suspend fun fetchRecordsToUpdateAndDeleteCountIfNecessary() {
        if(isDownSyncAllowed()) {
            fetchAndUpdateRecordsToDownSyncAndDeleteCount()
        }
    }

    private fun isDownSyncAllowed() = with(preferencesManager) {
        subjectsDownSyncSetting == ON || subjectsDownSyncSetting == EXTRA
    }

    internal suspend fun fetchAndUpdateRecordsToDownSyncAndDeleteCount() {
        try {
            val downSyncScope = subjectsDownSyncScopeRepository.getDownSyncScope()
            val counts = personRepository.countToDownSync(downSyncScope)
            recordsToDownSyncCountLiveData.postValue(counts.created)
            recordsToDeleteCountLiveData.postValue(counts.deleted)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    internal suspend fun fetchAndUpdateSelectedModulesCount() {
        selectedModulesCountLiveData.value = preferencesManager.selectedModules.map {
            ModuleCount(it,
                subjectLocalDataSource.count(SubjectLocalDataSource.Query(projectId = projectId, moduleId = it)))
        }
    }

    internal suspend fun fetchAndUpdatedUnselectedModulesCount() {
        val unselectedModules = subjectLocalDataSource.load(
            SubjectLocalDataSource.Query(projectId = projectId)
        ).filter { !preferencesManager.selectedModules.contains(it.moduleId) }
            .toList()
            .groupBy { it.moduleId }

        val unselectedModulesWithCount = unselectedModules.map {
            ModuleCount(it.key, it.value.size)
        }

        unselectedModulesCountLiveData.value = unselectedModulesWithCount
    }
}
