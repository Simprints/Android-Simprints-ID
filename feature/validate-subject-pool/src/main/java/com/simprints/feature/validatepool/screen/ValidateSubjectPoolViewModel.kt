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
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
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

    fun checkIdentificationPool(subjectQuery: SubjectQuery) = viewModelScope.launch {
        if (isSyncing) {
            return@launch
        }
        _state.send(ValidateSubjectPoolState.Validating)

        val validationState = when {
            hasRecords(subjectQuery) -> ValidateSubjectPoolState.Success
            subjectQuery.attendantId != null && hasRecords(SubjectQuery()) -> ValidateSubjectPoolState.UserMismatch
            subjectQuery.moduleId?.let { isModuleIdNotSynced(it) } == true -> ValidateSubjectPoolState.ModuleMismatch
            shouldSuggestSync() -> ValidateSubjectPoolState.RequiresSync
            else -> ValidateSubjectPoolState.PoolEmpty
        }

        _state.send(validationState)
    }

    fun syncAndRetry(subjectQuery: SubjectQuery) = viewModelScope.launch {
        _state.send(ValidateSubjectPoolState.SyncInProgress)
        isSyncing = true
        runBlockingSync()
        isSyncing = false
        checkIdentificationPool(subjectQuery)
    }

}

