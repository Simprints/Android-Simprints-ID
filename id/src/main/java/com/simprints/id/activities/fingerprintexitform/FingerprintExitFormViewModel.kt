package com.simprints.id.activities.fingerprintexitform

import androidx.lifecycle.ViewModel
import com.simprints.core.tools.extentions.inBackground
import com.simprints.eventsystem.event.domain.models.RefusalEvent
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer

class FingerprintExitFormViewModel(private val eventRepository: com.simprints.eventsystem.event.EventRepository) : ViewModel() {

    fun addExitFormEvent(startTime: Long, endTime: Long, otherText: String,
                         fingerprintExitFormReason: FingerprintExitFormReason) {
        inBackground {
            eventRepository.addOrUpdateEvent(RefusalEvent(startTime, endTime, fingerprintExitFormReason.toRefusalEventAnswer(), otherText))
        }
    }
}
