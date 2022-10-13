package com.simprints.id.activities.faceexitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.RefusalEvent
import com.simprints.id.data.exitform.FaceExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaceExitFormViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    @DispatcherIO private val dispatcher: CoroutineDispatcher
) :
    ViewModel() {

    fun addExitFormEvent(
        startTime: Long,
        endTime: Long,
        otherText: String,
        faceExitFormReason: FaceExitFormReason
    ) {
        viewModelScope.launch(dispatcher) {
            eventRepository.addOrUpdateEvent(
                RefusalEvent(
                    startTime,
                    endTime,
                    faceExitFormReason.toRefusalEventAnswer(),
                    otherText
                )
            )
            eventRepository.removeLocationDataFromCurrentSession()
        }
    }
}
