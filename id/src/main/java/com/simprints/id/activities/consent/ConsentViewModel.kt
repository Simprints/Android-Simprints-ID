package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import com.simprints.id.data.db.event.domain.models.ConsentEvent
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.ConsentEvent

class ConsentViewModel(private val sessionRepository: SessionRepository) : ViewModel() {

    fun addConsentEvent(consentEvent: ConsentEvent) {
        sessionRepository.addEventToCurrentSessionInBackground(consentEvent)
    }
}
