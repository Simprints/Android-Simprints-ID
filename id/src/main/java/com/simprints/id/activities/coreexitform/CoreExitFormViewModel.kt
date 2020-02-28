package com.simprints.id.activities.coreexitform

import androidx.lifecycle.ViewModel
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.RefusalEvent
import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer

class CoreExitFormViewModel(private val sessionRepository: SessionRepository): ViewModel() {

    fun addExitFormEvent(startTime: Long, endTime: Long, otherText: String, coreExitFormReason: CoreExitFormReason) {
        sessionRepository.addEventInBackground(RefusalEvent(startTime, endTime,
            coreExitFormReason.toRefusalEventAnswer(), otherText))
    }
}
