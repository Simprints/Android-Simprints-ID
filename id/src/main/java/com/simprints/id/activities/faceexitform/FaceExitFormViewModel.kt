package com.simprints.id.activities.faceexitform

import androidx.lifecycle.ViewModel
import com.simprints.core.tools.extentions.inBackground
import com.simprints.eventsystem.event.domain.models.RefusalEvent
import com.simprints.id.data.exitform.FaceExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer

class FaceExitFormViewModel(private val eventRepository: com.simprints.eventsystem.event.EventRepository) : ViewModel() {

    fun addExitFormEvent(startTime: Long, endTime: Long, otherText: String, faceExitFormReason: FaceExitFormReason) {
        inBackground {
            eventRepository.addOrUpdateEvent(
                RefusalEvent(startTime, endTime, faceExitFormReason.toRefusalEventAnswer(), otherText)
            )
            eventRepository.removeLocationDataFromCurrentSession()
        }
    }
}
