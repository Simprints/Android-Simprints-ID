package com.simprints.feature.validatepool.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.validatepool.ValidateSubjectPoolFragmentParams.ValidationMode
import com.simprints.feature.validatepool.usecase.HasRecordsUseCase
import com.simprints.feature.validatepool.usecase.IsModuleIdNotSyncedUseCase
import com.simprints.feature.validatepool.usecase.ShouldSuggestSyncUseCase
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ValidateSubjectPoolViewModel @Inject constructor(
    private val hasRecords: HasRecordsUseCase,
    private val isModuleIdNotSynced: IsModuleIdNotSyncedUseCase,
    private val shouldSuggestSync: ShouldSuggestSyncUseCase,
    private val syncOrchestrator: SyncOrchestrator,
    private val timeHelper: TimeHelper,
) : ViewModel() {
    private lateinit var cachedQuery: EnrolmentRecordQuery
    private lateinit var cachedMode: ValidationMode
    val state: LiveData<LiveDataEventWithContent<ValidateSubjectPoolState>>
        get() = _state
    private var _state = MutableLiveData<LiveDataEventWithContent<ValidateSubjectPoolState>>()
    private var isSyncing: Boolean = false

    private val syncStatusFlow = syncOrchestrator
        .observeSyncState()
        .filter { isSyncing }
        .map { it.eventSyncState }

    val lastSyncLabel: LiveData<LiveDataEventWithContent<String>>
        get() = _lastSyncLabel
    private var _lastSyncLabel = MutableLiveData<LiveDataEventWithContent<String>>()

    init {
        viewModelScope.launch {
            syncStatusFlow.collect { syncState ->
                if (syncState.isSyncReporterCompleted()) {
                    isSyncing = false
                    checkIdentificationPool(cachedQuery, cachedMode)
                }
            }
        }
        viewModelScope.launch {
            syncOrchestrator
                .observeSyncState()
                .map { it.eventSyncState.lastSyncTime }
                .filterNotNull()
                .collect { _lastSyncLabel.send(timeHelper.readableBetweenNowAndTime(it)) }
        }
    }

    fun checkIdentificationPool(
        enrolmentRecordQuery: EnrolmentRecordQuery,
        mode: ValidationMode,
    ) = viewModelScope.launch {
        if (isSyncing) {
            // In case of configuration change while syncing, we want to show the sync in progress state instead of default state
            _state.send(ValidateSubjectPoolState.SyncInProgress)
            return@launch
        }
        _state.send(ValidateSubjectPoolState.Validating)

        val validationState = when {
            // Check that any record are available as a shortcut
            hasRecords(enrolmentRecordQuery) -> ValidateSubjectPoolState.Success
            // Check attendant ID or module ID based on the ID configuration in the project
            hasInvalidAttendantId(enrolmentRecordQuery) -> ValidateSubjectPoolState.AttendantMismatch
            hasInvalidModuleId(enrolmentRecordQuery) -> ValidateSubjectPoolState.ModuleMismatch
            // Check for stale sync for a better result
            shouldSuggestSync() -> ValidateSubjectPoolState.RequiresSync
            // For Identification requests, no-records is a fail state
            mode == ValidationMode.IDENTIFICATION -> ValidateSubjectPoolState.PoolEmpty
            // For Enrol+, no-records is a success state
            else -> ValidateSubjectPoolState.Success
        }

        _state.send(validationState)
    }

    private suspend fun hasInvalidModuleId(enrolmentRecordQuery: EnrolmentRecordQuery): Boolean =
        enrolmentRecordQuery.moduleId?.let { isModuleIdNotSynced(it) } == true

    private suspend fun hasInvalidAttendantId(enrolmentRecordQuery: EnrolmentRecordQuery): Boolean =
        enrolmentRecordQuery.attendantId != null && hasRecords(EnrolmentRecordQuery())

    fun startSync(
        enrolmentRecordQuery: EnrolmentRecordQuery,
        mode: ValidationMode,
    ) = viewModelScope.launch {
        cachedQuery = enrolmentRecordQuery
        cachedMode = mode

        _state.send(ValidateSubjectPoolState.SyncInProgress)
        isSyncing = true
        syncOrchestrator.execute(OneTime.Events.start())
    }
}
