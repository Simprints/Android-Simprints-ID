package com.simprints.id.activities.fingerprintexitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.RefusalEvent
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FingerprintExitFormViewModel @Inject constructor(
    private val eventRepository: EventRepository,
) :
    ViewModel() {

    fun addExitFormEvent(
        startTime: Long, endTime: Long, otherText: String,
        fingerprintExitFormReason: FingerprintExitFormReason
    ) {
        viewModelScope.launch {
            eventRepository.addOrUpdateEvent(
                RefusalEvent(
                    startTime,
                    endTime,
                    fingerprintExitFormReason.toRefusalEventAnswer(),
                    otherText
                )
            )
            eventRepository.removeLocationDataFromCurrentSession()
        }
    }
}
