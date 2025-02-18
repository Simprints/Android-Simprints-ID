package com.simprints.feature.fetchsubject.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.fetchsubject.screen.usecase.FetchSubjectUseCase
import com.simprints.feature.fetchsubject.screen.usecase.SaveSubjectFetchEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FetchSubjectViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val fetchSubjectUseCase: FetchSubjectUseCase,
    private val saveSubjectFetchEventUseCase: SaveSubjectFetchEventUseCase,
) : ViewModel() {
    val subjectState: LiveData<LiveDataEventWithContent<FetchSubjectState>>
        get() = _subjectState
    private var _subjectState = MutableLiveData<LiveDataEventWithContent<FetchSubjectState>>()
    private var fetchWasAttempted = false

    fun onViewCreated(
        projectId: String,
        subjectId: String,
    ) {
        if (!fetchWasAttempted) {
            fetchSubject(projectId, subjectId)
        }
    }

    fun fetchSubject(
        projectId: String,
        subjectId: String,
    ) {
        viewModelScope.launch {
            fetchWasAttempted = true
            val subjectFetchStartTime = timeHelper.now()
            val subjectState = fetchSubjectUseCase(projectId, subjectId)

            _subjectState.send(subjectState)

            saveSubjectFetchEventUseCase(subjectState, subjectFetchStartTime, timeHelper.now(), subjectId)
        }
    }

    fun startExitForm() {
        _subjectState.send(FetchSubjectState.ShowExitForm)
    }
}
