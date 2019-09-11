package com.simprints.id.activities.exitform

import androidx.lifecycle.ViewModel
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent
import com.simprints.id.data.exitform.ExitFormReason.*
import com.simprints.id.data.exitform.toRefusalEventAnswer

class CoreExitFormViewModel(private val sessionEventsManager: SessionEventsManager): ViewModel() {

    var exitFormReason = OTHER

    fun handleReligiousConcernsRadioClick() {
        exitFormReason = REFUSED_RELIGION
    }

    fun handleDataConcernsRadioClick() {
        exitFormReason = REFUSED_DATA_CONCERNS
    }

    fun handlePersonNotPresentRadioClick() {
        exitFormReason = REFUSED_NOT_PRESENT
    }

    fun handleTooYoungRadioClick() {
        exitFormReason = REFUSED_YOUNG
    }

    fun handleDoesNotHavePermissionRadioClick() {
        exitFormReason = REFUSED_PERMISSION
    }

    fun handleAppNotWorkingRadioClick() {
        exitFormReason = SCANNER_NOT_WORKING
    }

    fun handleOtherRadioOptionClick() {
        exitFormReason = OTHER
    }

    fun addExitFormEvent(startTime: Long, endTime: Long, otherText: String) {
        sessionEventsManager.addEventInBackground(RefusalEvent(startTime, endTime,
            exitFormReason.toRefusalEventAnswer(), otherText))
    }
}
