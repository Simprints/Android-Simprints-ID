package com.simprints.feature.selectagegroup.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.SessionCoroutineScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.events.event.domain.models.AgeGroupSelectionEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SESSION
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SelectSubjectAgeGroupViewModel @Inject constructor(
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    private val buildAgeGroups: BuildAgeGroupsUseCase,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : ViewModel() {
    val finish: LiveData<LiveDataEventWithContent<AgeGroup>>
        get() = _finish
    private var _finish = MutableLiveData<LiveDataEventWithContent<AgeGroup>>()
    val ageGroups: LiveData<List<AgeGroup>>
        get() = _ageGroups
    private var _ageGroups = MutableLiveData<List<AgeGroup>>()
    private lateinit var startTime: Timestamp

    val showExitForm: LiveData<LiveDataEvent>
        get() = _showExitForm
    private val _showExitForm =
        MutableLiveData<LiveDataEvent>()

    fun start() = viewModelScope.launch {
        startTime = timeHelper.now()
        val ageGroups = buildAgeGroups()
        // notify the adapter
        _ageGroups.value = ageGroups
    }

    fun saveAgeGroupSelection(ageRange: AgeGroup) = sessionCoroutineScope.launch {
        val event = AgeGroupSelectionEvent(
            startTime,
            timeHelper.now(),
            AgeGroupSelectionEvent.AgeGroup(ageRange.startInclusive, ageRange.endExclusive),
        )
        eventRepository.addOrUpdateEvent(event)
        Simber.i("Added Age Group Selection Event", tag = SESSION)
        _finish.send(ageRange)
    }

    fun onBackPressed() {
        _showExitForm.send()
    }
}
