package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class SyncInformationViewModel(private val personRepository: PersonRepository,
                               private val personLocalDataSource: PersonLocalDataSource,
                               private val preferencesManager: PreferencesManager,
                               private val projectId: String,
                               private val peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository) : ViewModel() {

    val localRecordCount = MutableLiveData<Int>()
    val recordsToUpSyncCount = MutableLiveData<Int>()
    val recordsToDownSyncCount = MutableLiveData<Int>()
    val recordsToDeleteCount = MutableLiveData<Int>()
    val selectedModulesCount = MutableLiveData<List<ModuleCount>>()
    val unselectedModulesCount = MutableLiveData<List<ModuleCount>>()

    suspend fun start() {
        fetchAndUpdateLocalRecordCount()
        fetchAndUpdateRecordsToUpSyncCount()
        fetchAndUpdateRecordsToDownSyncAndDeleteCount()
        fetchAndUpdateSelectedModulesCount()
        fetchAndUpdatedUnselectedModulesCount()
    }

    internal suspend fun fetchAndUpdateLocalRecordCount() {
        localRecordCount.value = personLocalDataSource.count(PersonLocalDataSource.Query(projectId = projectId))
    }

    internal suspend fun fetchAndUpdateRecordsToUpSyncCount() {
        recordsToUpSyncCount.value = personLocalDataSource.count(PersonLocalDataSource.Query(toSync = true))
    }

    internal fun fetchAndUpdateRecordsToDownSyncAndDeleteCount() {
        viewModelScope.launch {
            try {
                val downSyncScope = peopleDownSyncScopeRepository.getDownSyncScope()
                val counts = personRepository.countToDownSync(downSyncScope)
                recordsToDownSyncCount.postValue(counts.sumBy {
                    it.created
                })
                recordsToDeleteCount.postValue(counts.sumBy {
                    it.deleted
                })
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    internal suspend fun fetchAndUpdateSelectedModulesCount() {
        selectedModulesCount.value = preferencesManager.selectedModules.map {
            ModuleCount(it,
                personLocalDataSource.count(PersonLocalDataSource.Query(projectId = projectId, moduleId = it)))
        }
    }

    internal suspend fun fetchAndUpdatedUnselectedModulesCount() {
        val unselectedModules = personLocalDataSource.load(
            PersonLocalDataSource.Query(projectId = projectId)
        ).toList()
            .groupBy { it.moduleId }
            .filterKeys { !preferencesManager.selectedModules.contains(it) }

        val unselectedModulesWithCount = unselectedModules.map {
            ModuleCount(it.key, it.value.size)
        }

        unselectedModulesCount.value = unselectedModulesWithCount
    }
}
