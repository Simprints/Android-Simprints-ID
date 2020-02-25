package com.simprints.id.activities.coreexitform

import androidx.lifecycle.ViewModel
import com.simprints.id.data.db.session.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.session.eventdata.models.domain.events.RefusalEvent
import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer

class CoreExitFormViewModel(private val sessionEventsManager: SessionEventsManager): ViewModel() {

    fun addExitFormEvent(startTime: Long, endTime: Long, otherText: String, coreExitFormReason: CoreExitFormReason) {
        sessionEventsManager.addEventInBackground(RefusalEvent(startTime, endTime,
            coreExitFormReason.toRefusalEventAnswer(), otherText))
    }
}
