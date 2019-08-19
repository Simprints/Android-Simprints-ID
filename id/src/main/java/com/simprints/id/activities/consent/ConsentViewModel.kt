package com.simprints.id.activities.consent

import android.util.Log
import androidx.lifecycle.*
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent
import com.simprints.id.data.consent.shortconsent.ConsentTextManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest

class ConsentViewModel(private val consentTextManager: ConsentTextManager,
                       private val sessionEventsManager: SessionEventsManager,
                       consentEvent: LiveData<ConsentEvent>) : ViewModel() {

    val appRequest = MutableLiveData<AppRequest>()
    var generalConsentText: LiveData<String>
    var parentalConsentText: LiveData<String>
    val  parentalConsentExists = consentTextManager.parentalConsentExists()

    val consentLiveData = MediatorLiveData<ConsentEvent>()

    var isConsentTabGeneral = true

    init {
        generalConsentText = Transformations.switchMap(appRequest) {
            consentTextManager.getGeneralConsentText(it)
        }

        parentalConsentText = Transformations.switchMap(appRequest) {
            consentTextManager.getParentalConsentText(it)
        }

        consentLiveData.addSource(consentEvent) {
            addConsentEvent(it)
        }
    }

    private fun addConsentEvent(consentEvent: ConsentEvent) {
        sessionEventsManager.addEventInBackground(consentEvent)
        Log.d("Generaleventlive", "Added consent event $consentEvent")
    }
}
