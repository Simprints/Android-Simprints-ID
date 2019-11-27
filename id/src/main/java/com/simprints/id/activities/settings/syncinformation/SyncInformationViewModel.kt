package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class SyncInformationViewModel(private val personRepository: PersonRepository,
                               private val personLocalDataSource: PersonLocalDataSource,
                               private val selectedSyncScope: SyncScope?) : ViewModel() {

    val localRecordCount = MutableLiveData<Int>()
    val recordsToUpSyncCount = MutableLiveData<Int>()
    val recordsToDownSyncCount = MutableLiveData<Int>()

    init {
        fetchAndUpdateLocalRecordCount()
        fetchAndUpdateRecordsToUpSyncCount()
        fetchAndUpdateRecordsToDownSyncCount()
    }

    private fun fetchAndUpdateLocalRecordCount() =
        selectedSyncScope?.let {syncScope ->
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
}
