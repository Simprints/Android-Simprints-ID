package com.simprints.id.activities.consent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent
import com.simprints.id.data.consent.shortconsent.ConsentTextManager

class ConsentViewModelFactory(private val consentTextManager: ConsentTextManager,
                              private val sessionEventsManager: SessionEventsManager) : ViewModelProvider.Factory {

    lateinit var consentEvents: MutableLiveData<ConsentEvent>

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConsentViewModel::class.java)) {
            ConsentViewModel(consentTextManager, sessionEventsManager, consentEvents) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
