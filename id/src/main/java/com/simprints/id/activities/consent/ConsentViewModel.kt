package com.simprints.id.activities.consent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.data.consent.shortconsent.ConsentRepository
import com.simprints.id.data.db.event.SessionRepository
import com.simprints.id.data.db.event.domain.events.ConsentEvent
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest

class ConsentViewModel(askConsentRequest: AskConsentRequest,
                       consentTextManager: ConsentRepository,
                       private val sessionRepository: SessionRepository) : ViewModel() {

    var generalConsentText: LiveData<String> = consentTextManager.getGeneralConsentText(askConsentRequest)
    var parentalConsentText: LiveData<String> = consentTextManager.getParentalConsentText(askConsentRequest)
    val  parentalConsentExists = consentTextManager.parentalConsentExists()

    fun addConsentEvent(consentEvent: ConsentEvent) {
        sessionRepository.addEventToCurrentSessionInBackground(consentEvent)
    }
}
