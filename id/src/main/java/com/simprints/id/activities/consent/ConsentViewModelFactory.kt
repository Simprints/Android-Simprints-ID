package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.consent.shortconsent.ConsentTextManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest

class ConsentViewModelFactory(private val consentTextManager: ConsentTextManager,
                              private val sessionEventsManager: SessionEventsManager) : ViewModelProvider.Factory {

    lateinit var appRequest: AppRequest

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConsentViewModel::class.java)) {
            ConsentViewModel(appRequest, consentTextManager, sessionEventsManager) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
