package com.simprints.id.activities.consent

import androidx.lifecycle.ViewModel
import com.simprints.core.tools.extentions.inBackground
import com.simprints.eventsystem.event.domain.models.ConsentEvent

class ConsentViewModel(private val eventRepository: com.simprints.eventsystem.event.EventRepository) : ViewModel() {

    fun addConsentEvent(consentEvent: ConsentEvent) {
        inBackground { eventRepository.addOrUpdateEvent(consentEvent) }
    }
}
