package com.simprints.id.activities.coreexitform

import androidx.lifecycle.ViewModel
import com.simprints.core.tools.extentions.inBackground
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.RefusalEvent
import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer

class CoreExitFormViewModel(private val eventRepository: EventRepository) : ViewModel() {

    fun addExitFormEvent(startTime: Long, endTime: Long, otherText: String, coreExitFormReason: CoreExitFormReason) {
        inBackground {
            eventRepository.addEvent(
                RefusalEvent(startTime, endTime, coreExitFormReason.toRefusalEventAnswer(), otherText)
            )
        }
    }
}
