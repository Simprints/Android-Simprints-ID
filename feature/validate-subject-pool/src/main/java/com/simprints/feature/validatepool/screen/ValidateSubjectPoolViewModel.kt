package com.simprints.feature.validatepool.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.validatepool.usecase.HasRecordsUseCase
import com.simprints.feature.validatepool.usecase.IsModuleIdNotSyncedUseCase
import com.simprints.feature.validatepool.usecase.ShouldSuggestSyncUseCase
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ValidateSubjectPoolViewModel @Inject constructor(
    private val hasRecords: HasRecordsUseCase,
    private val isModuleIdNotSynced: IsModuleIdNotSyncedUseCase,
    private val shouldSuggestSync: ShouldSuggestSyncUseCase,
    private val syncOrchestrator: SyncOrchestrator,
) : ViewModel() {
    private lateinit var cachedQuery: EnrolmentRecordQuery

    val state: LiveData<LiveDataEventWithContent<ValidateSubjectPoolState>>
        get() = _state
    private var _state = MutableLiveData<LiveDataEventWithContent<ValidateSubjectPoolState>>()
    private var isSyncing: Boolean = false

    private val syncStatusFlow = syncOrchestrator
        .observeSyncState()
        .filter { isSyncing }
        .map { it.eventSyncState }

    init {
        viewModelScope.launch {
            syncStatusFlow.collect { syncState ->
                if (syncState.isSyncReporterCompleted()) {
                    isSyncing = false
                    checkIdentificationPool(cachedQuery)
                }
            }
        }
    }

    fun checkIdentificationPool(enrolmentRecordQuery: EnrolmentRecordQuery) = viewModelScope.launch {
        if (isSyncing) {
            // In case of configuration change while syncing, we want to show the sync in progress state instead of default state
            _state.send(ValidateSubjectPoolState.SyncInProgress)
            return@launch
        }
        _state.send(ValidateSubjectPoolState.Validating)

        val validationState = when {
            hasRecords(enrolmentRecordQuery) -> ValidateSubjectPoolState.Success
            enrolmentRecordQuery.attendantId != null && hasRecords(EnrolmentRecordQuery()) -> ValidateSubjectPoolState.UserMismatch
            enrolmentRecordQuery.moduleId?.let { isModuleIdNotSynced(it) } == true -> ValidateSubjectPoolState.ModuleMismatch
            shouldSuggestSync() -> ValidateSubjectPoolState.RequiresSync
            else -> ValidateSubjectPoolState.PoolEmpty
        }

        _state.send(validationState)
    }

    fun startSync(enrolmentRecordQuery: EnrolmentRecordQuery) = viewModelScope.launch {
        cachedQuery = enrolmentRecordQuery
        _state.send(ValidateSubjectPoolState.SyncInProgress)
        isSyncing = true
        syncOrchestrator.execute(OneTime.Events.start())
    }
}
