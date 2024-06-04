package com.simprints.feature.selectagegroup.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.AgeGroupSelectionEvent
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
    @ExternalScope private val externalScope: CoroutineScope,

    ) : ViewModel() {

    val finish: LiveData<LiveDataEventWithContent<Boolean>>
        get() = _finish
    private var _finish = MutableLiveData<LiveDataEventWithContent<Boolean>>()
    val ageGroups: LiveData<List<AgeGroup>>
        get() = _ageGroups
    private var _ageGroups = MutableLiveData<List<AgeGroup>>()
    private lateinit var  startTime : Timestamp

    fun start() =
        viewModelScope.launch {
            startTime = timeHelper.now()
            val ageGroups = buildAgeGroups()
            // notify the adapter
            _ageGroups.value = ageGroups

        }

    fun saveAgeGroupSelection(ageRange: IntRange) = externalScope.launch {
        try {
            val event = AgeGroupSelectionEvent(
               startTime,
                timeHelper.now(),
                AgeGroupSelectionEvent.AgeGroup(ageRange.first, ageRange.last)
            )
            eventRepository.addOrUpdateEvent(event)

            Simber.tag(SESSION.name).i("Added Age Group Selection Event")
            _finish.send(true)
        } catch (t: Throwable) {
            // It doesn't matter if it was an error, we always return a result
            Simber.tag(SESSION.name).e(t)
            _finish.send(false)
        }
    }
}
