package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.R
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.tools.AndroidResourcesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class SyncInformationViewModel(private val personRepository: PersonRepository,
                               private val personLocalDataSource: PersonLocalDataSource,
                               private val preferencesManager: PreferencesManager,
                               private val androidResourcesHelper: AndroidResourcesHelper,
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
            val moduleCounts = ArrayList<ModuleCount>()
            moduleCounts.addAll(
                preferencesManager.selectedModules.map {
                    ModuleCount(it,
                        personLocalDataSource.count(PersonLocalDataSource.Query(projectId = projectId,
                        moduleId = it)))
                }
            )
            val totalRecordsEntry = ModuleCount(androidResourcesHelper.getString(R.string.sync_info_total_records),
                moduleCounts.sumBy { it.count })
            moduleCounts.add(TOTAL_RECORDS_INDEX, totalRecordsEntry)

            selectedModulesCount.value = moduleCounts
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
        val totalRecordsEntry = ModuleCount(androidResourcesHelper.getString(R.string.sync_info_total_records),
            unselectedModulesWithCount.sumBy { it.count })
        unselectedModulesWithCount.add(TOTAL_RECORDS_INDEX, totalRecordsEntry)

        unselectedModulesCount.value = unselectedModulesWithCount.filter { it.count > 0 }
    }

    companion object {
        private const val TOTAL_RECORDS_INDEX = 0
    }
}
