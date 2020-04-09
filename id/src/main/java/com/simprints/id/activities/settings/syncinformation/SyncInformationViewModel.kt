package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.common.models.EventType
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncSetting.EXTRA
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncSetting.ON
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class SyncInformationViewModel(private val personRepository: PersonRepository,
                               private val personLocalDataSource: PersonLocalDataSource,
                               private val preferencesManager: PreferencesManager,
                               private val projectId: String,
                               private val peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository) : ViewModel() {

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
        localRecordCountLiveData.value = personLocalDataSource.count(PersonLocalDataSource.Query(projectId = projectId))
    }

    internal suspend fun fetchAndUpdateRecordsToUpSyncCount() {
        recordsToUpSyncCountLiveData.value = personLocalDataSource.count(PersonLocalDataSource.Query(toSync = true))
    }

    internal suspend fun fetchRecordsToUpdateAndDeleteCountIfNecessary() {
        if(isDownSyncAllowed()) {
            fetchAndUpdateRecordsToDownSyncAndDeleteCount()
        }
    }

    private fun isDownSyncAllowed() = with(preferencesManager) {
        peopleDownSyncSetting == ON || peopleDownSyncSetting == EXTRA
    }

    internal suspend fun fetchAndUpdateRecordsToDownSyncAndDeleteCount() {
        try {
            val downSyncScope = peopleDownSyncScopeRepository.getDownSyncScope()
            val counts = personRepository.countToDownSync(downSyncScope)
            recordsToDownSyncCountLiveData.postValue(counts.filter {
                it.type == EventType.EnrolmentRecordCreation
            }.sumBy { it.count })
            recordsToDeleteCountLiveData.postValue(counts.filter {
                it.type == EventType.EnrolmentRecordDeletion
            }.sumBy { it.count })
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    internal suspend fun fetchAndUpdateSelectedModulesCount() {
        selectedModulesCountLiveData.value = preferencesManager.selectedModules.map {
            ModuleCount(it,
                personLocalDataSource.count(PersonLocalDataSource.Query(projectId = projectId, moduleId = it)))
        }
    }

    internal suspend fun fetchAndUpdatedUnselectedModulesCount() {
        val unselectedModules = personLocalDataSource.load(
            PersonLocalDataSource.Query(projectId = projectId)
        ).filter { !preferencesManager.selectedModules.contains(it.moduleId) }
            .toList()
            .groupBy { it.moduleId }

        val unselectedModulesWithCount = unselectedModules.map {
            ModuleCount(it.key, it.value.size)
        }

        unselectedModulesCountLiveData.value = unselectedModulesWithCount
    }
}
