package com.simprints.id.activities.exitform

import androidx.lifecycle.ViewModel
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent
import com.simprints.id.data.exitform.ExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer

class CoreExitFormViewModel(private val sessionEventsManager: SessionEventsManager): ViewModel() {

    fun addExitFormEvent(startTime: Long, endTime: Long, otherText: String, exitFormReason: ExitFormReason) {
        sessionEventsManager.addEventInBackground(RefusalEvent(startTime, endTime,
            exitFormReason.toRefusalEventAnswer(), otherText))
    }
}
