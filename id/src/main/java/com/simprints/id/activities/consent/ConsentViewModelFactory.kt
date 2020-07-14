package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.consent.shortconsent.ConsentRepository
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest

class ConsentViewModelFactory(private val consentTextManager: ConsentRepository,
                              private val eventRepository: EventRepository) : ViewModelProvider.Factory {

    lateinit var askConsentRequest: AskConsentRequest

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ConsentViewModel::class.java)) {
            ConsentViewModel(askConsentRequest, consentTextManager, eventRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
