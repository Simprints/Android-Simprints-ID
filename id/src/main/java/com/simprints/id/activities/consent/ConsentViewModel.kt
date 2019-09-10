package com.simprints.id.activities.consent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent
import com.simprints.id.data.consent.shortconsent.ConsentRepository
import com.simprints.id.domain.moduleapi.app.requests.AppRequest

class ConsentViewModel(appRequest: AppRequest,
                       consentTextManager: ConsentRepository,
                       private val sessionEventsManager: SessionEventsManager) : ViewModel() {

    var generalConsentText: LiveData<String> = consentTextManager.getGeneralConsentText(appRequest)
    var parentalConsentText: LiveData<String> = consentTextManager.getParentalConsentText(appRequest)
    val  parentalConsentExists = consentTextManager.parentalConsentExists()

    fun addConsentEvent(consentEvent: ConsentEvent) {
        sessionEventsManager.addEventInBackground(consentEvent)
    }
}
