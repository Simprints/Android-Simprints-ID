package com.simprints.id.activities.faceexitform

import androidx.lifecycle.ViewModel
import com.simprints.core.ExternalScope
import com.simprints.id.data.exitform.FaceExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.RefusalEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaceExitFormViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope
) :
    ViewModel() {

    fun addExitFormEvent(
        startTime: Long,
        endTime: Long,
        otherText: String,
        faceExitFormReason: FaceExitFormReason
    ) {
        externalScope.launch {
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
