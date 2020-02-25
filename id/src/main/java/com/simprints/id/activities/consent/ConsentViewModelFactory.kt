package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.consent.shortconsent.ConsentRepository
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest

class ConsentViewModelFactory(private val consentTextManager: ConsentRepository,
                              private val sessionEventsManager: SessionEventsManager) : ViewModelProvider.Factory {

    lateinit var askConsentRequest: AskConsentRequest

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConsentViewModel::class.java)) {
            ConsentViewModel(askConsentRequest, consentTextManager, sessionEventsManager) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
