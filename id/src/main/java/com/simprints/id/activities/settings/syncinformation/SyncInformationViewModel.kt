package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class SyncInformationViewModel(private val personRepository: PersonRepository,
                               private val personLocalDataSource: PersonLocalDataSource,
                               private val preferencesManager: PreferencesManager,
                               private val projectId: String,
                               private val syncScopesBuilder: SyncScopesBuilder) : ViewModel() {

    val localRecordCount = MutableLiveData<Int>()
    val recordsToUpSyncCount = MutableLiveData<Int>()
    val recordsToDownSyncCount = MutableLiveData<Int>()
    val selectedModulesCount = MutableLiveData<List<ModuleCount>>()
    val unselectedModulesCount = MutableLiveData<List<ModuleCount>>()

    fun start() {
        fetchAndUpdateLocalRecordCount()
        fetchAndUpdateRecordsToUpSyncCount()
        fetchAndUpdateRecordsToDownSyncCount()
        fetchAndUpdateSelectedModulesCount()
        fetchAndUpdatedUnselectedModulesCount()
    }

    internal fun fetchAndUpdateLocalRecordCount() {
        localRecordCount.value = personLocalDataSource.count(PersonLocalDataSource.Query(projectId = projectId))
    }

    private fun fetchAndUpdateRecordsToUpSyncCount() {
        recordsToUpSyncCount.value = personLocalDataSource.count(PersonLocalDataSource.Query(toSync = true))
    }

    internal fun fetchAndUpdateRecordsToDownSyncCount() =
        syncScopesBuilder.buildSyncScope()?.let { syncScope ->
            personRepository.countToDownSync(syncScope)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy { peopleCounts ->
                    recordsToDownSyncCount.postValue(peopleCounts.sumBy {
                        it.count
                    })
                }
        }

    internal fun fetchAndUpdateSelectedModulesCount() {
        selectedModulesCount.value = preferencesManager.selectedModules.map {
            ModuleCount(it,
                personLocalDataSource.count(PersonLocalDataSource.Query(projectId = projectId, moduleId = it)))
        }
    }

    private fun fetchAndUpdatedUnselectedModulesCount() {
        val unselectedModules = preferencesManager.moduleIdOptions.minus(preferencesManager.selectedModules)
        val unselectedModulesWithCount = unselectedModules.map {
            ModuleCount(it, personLocalDataSource.count(PersonLocalDataSource.Query(
                projectId = projectId,
                moduleId = it))) }

        unselectedModulesCount.value = unselectedModulesWithCount.filter { it.count > 0 }
    }
}
