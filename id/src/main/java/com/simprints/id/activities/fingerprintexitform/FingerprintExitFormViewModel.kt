package com.simprints.id.activities.fingerprintexitform

import androidx.lifecycle.ViewModel
import com.simprints.core.tools.extentions.inBackground
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.RefusalEvent
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer

class FingerprintExitFormViewModel(private val eventRepository: EventRepository) : ViewModel() {

    fun addExitFormEvent(startTime: Long, endTime: Long, otherText: String,
                         fingerprintExitFormReason: FingerprintExitFormReason) {
        inBackground {
            eventRepository.addEvent(RefusalEvent(startTime, endTime, fingerprintExitFormReason.toRefusalEventAnswer(), otherText))
        }
    }
}
