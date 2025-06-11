package com.simprints.feature.troubleshooting.recordsmigration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.troubleshooting.adapter.TroubleshootingItemViewData
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RealmToRoomRecordsMigrationViewModel @Inject constructor(
    private val flagStore: RealmToRoomMigrationFlagsStore,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
) : ViewModel() {
    private val _logs = MutableLiveData<List<TroubleshootingItemViewData>>(emptyList())
    val logs: LiveData<List<TroubleshootingItemViewData>>
        get() = _logs

    fun collectData() {
        viewModelScope.launch {
            _logs.postValue(
                listOf(
                    TroubleshootingItemViewData(
                        title = "Realm to Room migration flags:",
                        body = flagStore.getStoreStateAsString(),
                    ),
                    TroubleshootingItemViewData(
                        title = "Local db info",
                        body = enrolmentRecordRepository.getLocalDBInfo(),
                    ),
                    TroubleshootingItemViewData(
                        title = "Generate a migration failure report",
                        body = "To delete the new db files and generate a migration failure report.\n " +
                            "click details button.",
                        navigationId = "delete_realm_files_and_generate_report",
                    ),
                ),
            )
        }
    }
}
