package com.simprints.id.activities.fingerprintexitform

import androidx.lifecycle.ViewModel
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.data.db.session.domain.models.events.RefusalEvent
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer

class FingerprintExitFormViewModel(private val sessionEventsManager: SessionEventsManager) : ViewModel() {

    fun addExitFormEvent(startTime: Long, endTime: Long, otherText: String,
                         fingerprintExitFormReason: FingerprintExitFormReason) {
        sessionEventsManager.addEventInBackground(RefusalEvent(startTime, endTime,
            fingerprintExitFormReason.toRefusalEventAnswer(), otherText))
    }
}
