package com.simprints.feature.validatepool.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.validatepool.usecase.HasRecordsUseCase
import com.simprints.feature.validatepool.usecase.IsModuleIdNotSyncedUseCase
import com.simprints.feature.validatepool.usecase.RunBlockingEventSyncUseCase
import com.simprints.feature.validatepool.usecase.ShouldSuggestSyncUseCase
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ValidateSubjectPoolViewModel @Inject constructor(
    private val hasRecords: HasRecordsUseCase,
    private val isModuleIdNotSynced: IsModuleIdNotSyncedUseCase,
    private val shouldSuggestSync: ShouldSuggestSyncUseCase,
    private val runBlockingSync: RunBlockingEventSyncUseCase,
) : ViewModel() {
    val state: LiveData<LiveDataEventWithContent<ValidateSubjectPoolState>>
        get() = _state
    private var _state = MutableLiveData<LiveDataEventWithContent<ValidateSubjectPoolState>>()
    private var isSyncing: Boolean = false

    fun checkIdentificationPool(enrolmentRecordQuery: EnrolmentRecordQuery) = viewModelScope.launch {
        if (isSyncing) {
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

    fun syncAndRetry(enrolmentRecordQuery: EnrolmentRecordQuery) = viewModelScope.launch {
        _state.send(ValidateSubjectPoolState.SyncInProgress)
        isSyncing = true
        runBlockingSync()
        isSyncing = false
        checkIdentificationPool(enrolmentRecordQuery)
    }
}
