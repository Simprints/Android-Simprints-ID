package com.simprints.id.activities.consent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent
import com.simprints.id.data.consent.shortconsent.ConsentTextManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest

class ConsentViewModel(private val consentTextManager: ConsentTextManager,
                       private val sessionEventsManager: SessionEventsManager) : ViewModel() {

    val appRequest by lazy {  MutableLiveData<AppRequest>() }
    var generalConsentText: LiveData<String>
    var parentalConsentText: LiveData<String>
    val  parentalConsentExists = consentTextManager.parentalConsentExists()

    init {
        generalConsentText = Transformations.switchMap(appRequest) {
            consentTextManager.getGeneralConsentText(it)
        }

        parentalConsentText = Transformations.switchMap(appRequest) {
            consentTextManager.getParentalConsentText(it)
        }
    }

    fun addConsentEvent(consentEvent: ConsentEvent) {
        sessionEventsManager.addEventInBackground(consentEvent)
    }
}
