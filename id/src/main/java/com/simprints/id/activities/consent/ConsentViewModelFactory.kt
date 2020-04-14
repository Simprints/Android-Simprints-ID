package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.consent.shortconsent.ConsentRepository
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest

class ConsentViewModelFactory(private val consentTextManager: ConsentRepository,
                              private val sessionRepository: SessionRepository) : ViewModelProvider.Factory {

    lateinit var askConsentRequest: AskConsentRequest

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConsentViewModel::class.java)) {
            ConsentViewModel(askConsentRequest, consentTextManager, sessionRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
