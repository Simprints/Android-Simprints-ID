package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class SyncInformationViewModel(private val personRepository: PersonRepository,
                               private val personLocalDataSource: PersonLocalDataSource,
                               private val preferencesManager: PreferencesManager,
                               private val projectId: String,
                               private val selectedSyncScope: SyncScope?) : ViewModel() {

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

    private fun fetchAndUpdateLocalRecordCount() =
        selectedSyncScope?.let { syncScope ->
            personRepository.localCountForSyncScope(syncScope).subscribeBy { peopleCounts ->
                localRecordCount.postValue(peopleCounts.sumBy {
                    it.count
                })
            }
        }

    private fun fetchAndUpdateRecordsToUpSyncCount() =
        recordsToUpSyncCount.postValue(
            personLocalDataSource.count(PersonLocalDataSource.Query(toSync = true))
        )

    private fun fetchAndUpdateRecordsToDownSyncCount() =
        selectedSyncScope?.let { syncScope ->
            personRepository.countToDownSync(syncScope)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy { peopleCounts ->
                    recordsToDownSyncCount.postValue(peopleCounts.sumBy {
                        it.count
                    })
                }
        }

    private fun fetchAndUpdateSelectedModulesCount() {
            val moduleCounts = ArrayList<ModuleCount>()
            moduleCounts.addAll(
                preferencesManager.selectedModules.map {
                    ModuleCount(it,
                        personLocalDataSource.count(PersonLocalDataSource.Query(projectId = projectId,
                        moduleId = it)))
                }
            )
            val totalEntry = ModuleCount("Total records", moduleCounts.sumBy { it.count })
            moduleCounts.add(0, totalEntry)

            selectedModulesCount.postValue(moduleCounts)
    }

    private fun fetchAndUpdatedUnselectedModulesCount() {
        val unselectedModules = preferencesManager.moduleIdOptions.minus(preferencesManager.selectedModules)
        val unselectedModulesWithCount = ArrayList<ModuleCount>()

        unselectedModulesWithCount.addAll(unselectedModules.map {
            ModuleCount(it, personLocalDataSource.count(PersonLocalDataSource.Query(
                projectId = projectId,
                moduleId = it)))
        }
        )
        val totalEntry = ModuleCount("Total records", unselectedModulesWithCount.sumBy { it.count })
        unselectedModulesWithCount.add(0, totalEntry)

        unselectedModulesCount.postValue(unselectedModulesWithCount.filter { it.count > 0 })
    }
}
